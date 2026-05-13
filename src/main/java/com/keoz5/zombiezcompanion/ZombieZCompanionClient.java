package com.keoz5.zombiezcompanion;

import com.keoz5.zombiezcompanion.config.ConfigManager;
import com.keoz5.zombiezcompanion.events.InternalEvent;
import com.keoz5.zombiezcompanion.events.InternalEventBus;
import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.modules.IModule;
import com.keoz5.zombiezcompanion.modules.ModuleRegistry;
import com.keoz5.zombiezcompanion.modules.alerts.EventAlertsModule;
import com.keoz5.zombiezcompanion.modules.hud.HudState;
import com.keoz5.zombiezcompanion.modules.hud.SmartHudModule;
import com.keoz5.zombiezcompanion.modules.tracker.SessionManager;
import com.keoz5.zombiezcompanion.modules.tracker.SessionTrackerModule;
import com.keoz5.zombiezcompanion.parser.BossBarParser;
import com.keoz5.zombiezcompanion.parser.ChatMessageParser;
import com.keoz5.zombiezcompanion.parser.ParsedEventInfo;
import com.keoz5.zombiezcompanion.parser.ScoreboardParser;
import com.keoz5.zombiezcompanion.storage.SessionHistoryStorage;
import com.keoz5.zombiezcompanion.ui.MainConfigScreen;
import com.keoz5.zombiezcompanion.util.DebugLogger;
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
 *  2. Init DebugLogger (wires ModConfig reference)
 *  3. Instantiate modules (they subscribe to eventBus in constructors)
 *  4. Register Fabric event callbacks
 *  5. Register keybind (Right Shift) + /zzc command
 *
 * ── HUD rendering API ─────────────────────────────────────────────────────
 * Target: Fabric API 0.114.0+1.21.4  →  HudRenderCallback signature:
 *   (DrawContext, RenderTickCounter)
 * where RenderTickCounter is net.minecraft.client.render.RenderTickCounter (Yarn).
 * Call tickCounter.getTickDelta(true) to get the partial tick as a float.
 *
 * If you downgrade fabric-api below 0.100, change to:
 *   HudRenderCallback.EVENT.register((ctx, delta) -> moduleRegistry.onHudRender(ctx, delta));
 * ─────────────────────────────────────────────────────────────────────────
 *
 * ── Keybind ───────────────────────────────────────────────────────────────
 * Default: Right Shift (GLFW_KEY_RIGHT_SHIFT). Rebindable in Controls screen.
 * ─────────────────────────────────────────────────────────────────────────
 *
 * ── Debug mode ───────────────────────────────────────────────────────────
 * Toggle with /zzc debug or in the config screen.
 * Periodic debug (every ~5 s): scoreboard lines, bossbars, HUD state.
 * Every chat message: [ZombieZ][DEBUG][Chat:Raw]
 * See CLAUDE.md §"Procédure de collecte des données in-game".
 * ─────────────────────────────────────────────────────────────────────────
 */
public final class ZombieZCompanionClient implements ClientModInitializer {

    public static final String MOD_ID = "zombiezcompanion";

    private static ConfigManager    configManager;
    private static ModuleRegistry   moduleRegistry;
    private static InternalEventBus eventBus;

    // Throttle periodic debug logging to once every ~5 s (100 ticks)
    private int debugTickCounter = 0;
    private static final int DEBUG_INTERVAL_TICKS = 100;

    @Override
    public void onInitializeClient() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

        configManager = new ConfigManager(configDir);
        DebugLogger.init(configManager.getConfig());   // wire debug flag reference

        eventBus       = new InternalEventBus();
        moduleRegistry = new ModuleRegistry();

        // ── Build modules ─────────────────────────────────────────────────
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

        // ── Fabric events ─────────────────────────────────────────────────
        registerFabricEvents();
        registerKeybind();
        registerClientCommand();

        ModLogger.info("ZombieZ Companion initialized ("
                + moduleRegistry.getModules().size() + " modules). "
                + "Debug mode: " + configManager.getConfig().debugMode);
    }

    // ── Fabric events ────────────────────────────────────────────────────

    private void registerFabricEvents() {

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            moduleRegistry.onTick(client);
            runPeriodicDebug(client);
        });

        // Fabric API >= 0.100: callback is (DrawContext, RenderTickCounter)
        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                moduleRegistry.onHudRender(drawContext, tickCounter.getTickDelta(true)));

        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, timestamp) -> {
            moduleRegistry.onChatMessage(message, false);
            dispatchParsedEvent(message);
        });

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                moduleRegistry.onChatMessage(message, true);
                dispatchParsedEvent(message);
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                moduleRegistry.onGameJoin());

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            moduleRegistry.onGameLeave();
            configManager.save();
        });
    }

    /** Parse chat text and broadcast typed events on the internal bus. */
    private static void dispatchParsedEvent(Text message) {
        String raw = TextUtils.plain(message);
        ParsedEventInfo info = ChatMessageParser.parseEvent(raw);
        if (info != null) {
            // [ZombieZ][DEBUG][Event] already logged inside parseEvent via chatParsed
            DebugLogger.event(info.type().name(), info.displayName(), raw);
            eventBus.publish(new InternalEvent(info.type(), info));
        }
        if (ChatMessageParser.isKillMessage(raw)) {
            // [ZombieZ][DEBUG][Kill] already logged inside isKillMessage
            eventBus.publish(new InternalEvent(ServerEventType.KILL_DETECTED));
        }
    }

    // ── Periodic debug logging (every 100 ticks ≈ 5 s) ──────────────────

    private void runPeriodicDebug(MinecraftClient client) {
        if (!DebugLogger.isEnabled()) return;
        if (client.world == null) return;
        if (++debugTickCounter < DEBUG_INTERVAL_TICKS) return;
        debugTickCounter = 0;

        // 1. Scoreboard sidebar
        ScoreboardParser.debugPrintSidebar(client.world.getScoreboard());

        // 2. Active bossbars
        BossBarParser.debugPrintBossBars(client.inGameHud.getBossBarHud());

        // 3. Current HUD state (parsed values)
        moduleRegistry.getModule(SmartHudModule.class).ifPresent(hud -> {
            HudState s = hud.getHudState();
            DebugLogger.hudState("zone",           s.zone);
            DebugLogger.hudState("class",          s.playerClass);
            DebugLogger.hudState("mutationReady",  String.valueOf(s.mutationReady));
            DebugLogger.hudState("mutationName",   s.mutationName);
            DebugLogger.hudState("streak",         String.valueOf(s.streak));
            DebugLogger.hudState("activeEvent",    s.activeEvent);
            DebugLogger.hudState("sessionStartMs", String.valueOf(s.sessionStartMs));
        });
    }

    // ── Keybind (Right Shift) ─────────────────────────────────────────────

    private static KeyBinding openMenuKey;

    private void registerKeybind() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zombiezcompanion.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
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

    // ── Client command /zzc ───────────────────────────────────────────────

    private void registerClientCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
            dispatcher.register(ClientCommandManager.literal("zzc")

                .then(ClientCommandManager.literal("menu").executes(ctx -> {
                    MinecraftClient.getInstance().setScreen(
                            new MainConfigScreen(null, configManager, moduleRegistry));
                    return 1;
                }))

                .then(ClientCommandManager.literal("debug").executes(ctx -> {
                    boolean next = !configManager.getConfig().debugMode;
                    configManager.getConfig().debugMode = next;
                    configManager.save();
                    String state = next ? "§aactivé" : "§cdésactivé";
                    ctx.getSource().sendFeedback(
                            Text.literal("[ZZC] Mode debug " + state + "§r. "
                                    + (next ? "Filtre log: [ZombieZ][DEBUG]" : "")));
                    ModLogger.info("[ZZC] Debug mode " + (next ? "ON" : "OFF"));
                    return 1;
                }))

                .then(ClientCommandManager.literal("status").executes(ctx -> {
                    StringBuilder sb = new StringBuilder("[ZZC] Modules:\n");
                    for (IModule m : moduleRegistry.getModules()) {
                        sb.append("  • ").append(m.getId())
                          .append(": ").append(m.isEnabled() ? "§aON" : "§cOFF").append("§r\n");
                    }
                    sb.append("Debug: ").append(configManager.getConfig().debugMode ? "§aON" : "§cOFF");
                    ctx.getSource().sendFeedback(Text.literal(sb.toString()));
                    return 1;
                }))
            )
        );
    }

    // ── Static accessors ─────────────────────────────────────────────────

    public static ConfigManager    getConfigManager()  { return configManager; }
    public static ModuleRegistry   getModuleRegistry() { return moduleRegistry; }
    public static InternalEventBus getEventBus()       { return eventBus; }
}
