package com.keoz5.zombiezcompanion;

import com.keoz5.zombiezcompanion.config.ConfigManager;
import com.keoz5.zombiezcompanion.events.InternalEvent;
import com.keoz5.zombiezcompanion.events.InternalEventBus;
import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.modules.IModule;
import com.keoz5.zombiezcompanion.modules.ModuleRegistry;
import com.keoz5.zombiezcompanion.modules.alerts.EventAlertsModule;
import com.keoz5.zombiezcompanion.modules.hud.SmartHudModule;
import com.keoz5.zombiezcompanion.modules.tracker.SessionManager;
import com.keoz5.zombiezcompanion.modules.tracker.SessionTrackerModule;
import com.keoz5.zombiezcompanion.parser.ChatMessageParser;
import com.keoz5.zombiezcompanion.parser.ParsedEventInfo;
import com.keoz5.zombiezcompanion.parser.ScoreboardParser;
import com.keoz5.zombiezcompanion.storage.SessionHistoryStorage;
import com.keoz5.zombiezcompanion.ui.MainConfigScreen;
import com.keoz5.zombiezcompanion.util.ModLogger;
import com.keoz5.zombiezcompanion.util.TextUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

/**
 * Client entry-point for ZombieZ Companion.
 *
 * Wiring order:
 *  1. Load config from disk
 *  2. Instantiate modules (they subscribe to eventBus in constructors)
 *  3. Register Fabric event callbacks
 *  4. Register keybind + client command /zzc
 *
 * ── HUD rendering note ───────────────────────────────────────────────────
 * We use HudRenderCallback from fabric-rendering-v1.
 * For Fabric API >= 0.100 (which includes 0.114.0+1.21.4), the callback
 * signature is: (DrawContext, RenderTickCounter).
 * net.minecraft.client.render.RenderTickCounter is the Yarn-mapped class.
 * If you downgrade fabric-api below 0.100, replace RenderTickCounter with
 * a float parameter and remove .getTickDelta(true).
 * ─────────────────────────────────────────────────────────────────────────
 *
 * ── Keybind note ─────────────────────────────────────────────────────────
 * Default open-menu key: Right Shift (GLFW_KEY_RIGHT_SHIFT).
 * Rebindable in Minecraft Options → Controls → ZombieZ Companion.
 * ─────────────────────────────────────────────────────────────────────────
 */
public final class ZombieZCompanionClient implements ClientModInitializer {

    public static final String MOD_ID = "zombiezcompanion";

    private static ConfigManager    configManager;
    private static ModuleRegistry   moduleRegistry;
    private static InternalEventBus eventBus;

    // Counts ticks to throttle per-tick debug logging (every 100 ticks ≈ 5 s)
    private int debugTickCounter = 0;
    private static final int DEBUG_INTERVAL_TICKS = 100;

    @Override
    public void onInitializeClient() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

        configManager  = new ConfigManager(configDir);
        eventBus       = new InternalEventBus();
        moduleRegistry = new ModuleRegistry();

        // ---- Build modules -----------------------------------------------
        SessionHistoryStorage historyStorage = new SessionHistoryStorage(configDir);
        SessionManager sessionManager = new SessionManager(
                configManager.getConfig().sessionTracker, eventBus, historyStorage);

        moduleRegistry.register(new EventAlertsModule(configManager.getConfig().eventAlerts, eventBus));
        moduleRegistry.register(new SmartHudModule(configManager.getConfig().smartHud, eventBus));
        moduleRegistry.register(new SessionTrackerModule(
                configManager.getConfig().sessionTracker, sessionManager, eventBus));

        for (IModule module : moduleRegistry.getModules()) {
            module.onInitialize(configManager.getConfig());
        }

        // ---- Fabric events -----------------------------------------------
        registerFabricEvents();
        registerKeybind();
        registerClientCommand();

        ModLogger.info("ZombieZ Companion initialized ("
                + moduleRegistry.getModules().size() + " modules).");
    }

    // ---- Fabric events --------------------------------------------------

    private void registerFabricEvents() {

        // Client tick — also drives the scoreboard debug logger
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            moduleRegistry.onTick(client);
            debugScoreboardTick(client);
        });

        // HUD — Fabric API >= 0.100 (included in 0.114.0+1.21.4):
        // RenderTickCounter is net.minecraft.client.render.RenderTickCounter (Yarn mapping)
        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                moduleRegistry.onHudRender(drawContext, tickCounter.getTickDelta(true)));

        // Player chat
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, timestamp) -> {
            moduleRegistry.onChatMessage(message, false);
            dispatchParsedEvent(message);
        });

        // System / game messages — skip action-bar (overlay = true)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                moduleRegistry.onChatMessage(message, true);
                dispatchParsedEvent(message);
            }
        });

        // Connection lifecycle
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                moduleRegistry.onGameJoin());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            moduleRegistry.onGameLeave();
            configManager.save();
        });
    }

    /** Parse raw text and broadcast a typed event on the internal bus. */
    private static void dispatchParsedEvent(Text message) {
        String raw = TextUtils.plain(message);

        ParsedEventInfo info = ChatMessageParser.parseEvent(raw);
        if (info != null) {
            if (configManager.getConfig().debugMode) {
                ModLogger.debug("[EventBus] Dispatching " + info.type()
                        + " – \"" + info.displayName() + "\" from: \"" + raw + "\"");
            }
            eventBus.publish(new InternalEvent(info.type(), info));
        }

        if (ChatMessageParser.isKillMessage(raw)) {
            eventBus.publish(new InternalEvent(ServerEventType.KILL_DETECTED));
        }
    }

    /** Logs scoreboard lines every DEBUG_INTERVAL_TICKS when debugMode is on. */
    private void debugScoreboardTick(MinecraftClient client) {
        if (!configManager.getConfig().debugMode) return;
        if (client.world == null) return;
        if (++debugTickCounter < DEBUG_INTERVAL_TICKS) return;
        debugTickCounter = 0;
        ScoreboardParser.debugPrintSidebar(client.world.getScoreboard());
    }

    // ---- Keybind (Right Shift) ------------------------------------------

    private static KeyBinding openMenuKey;

    private void registerKeybind() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zombiezcompanion.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,   // Right Shift — rebindable in Controls
                "key.categories.zombiezcompanion"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new MainConfigScreen(null, configManager, moduleRegistry));
                }
            }
        });
    }

    // ---- Client command /zzc --------------------------------------------

    private void registerClientCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
            dispatcher.register(ClientCommandManager.literal("zzc")

                .then(ClientCommandManager.literal("menu").executes(ctx -> {
                    MinecraftClient.getInstance().setScreen(
                            new MainConfigScreen(null, configManager, moduleRegistry));
                    return 1;
                }))

                .then(ClientCommandManager.literal("debug").executes(ctx -> {
                    boolean current = configManager.getConfig().debugMode;
                    configManager.getConfig().debugMode = !current;
                    configManager.save();
                    String state = configManager.getConfig().debugMode ? "activé" : "désactivé";
                    ctx.getSource().sendFeedback(
                            Text.literal("[ZZC] Mode debug " + state + "."));
                    ModLogger.info("[ZZC] Debug mode " + state);
                    return 1;
                }))

                .then(ClientCommandManager.literal("status").executes(ctx -> {
                    StringBuilder sb = new StringBuilder("[ZZC] Modules:\n");
                    for (IModule m : moduleRegistry.getModules()) {
                        sb.append("  • ").append(m.getId())
                          .append(": ").append(m.isEnabled() ? "ON" : "OFF").append("\n");
                    }
                    ctx.getSource().sendFeedback(Text.literal(sb.toString()));
                    ModLogger.info(sb.toString());
                    return 1;
                }))
            )
        );
    }

    // ---- Static accessors -----------------------------------------------

    public static ConfigManager    getConfigManager()  { return configManager; }
    public static ModuleRegistry   getModuleRegistry() { return moduleRegistry; }
    public static InternalEventBus getEventBus()       { return eventBus; }
}
