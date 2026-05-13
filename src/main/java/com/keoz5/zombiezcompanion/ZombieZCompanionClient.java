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
 *  1. Load config
 *  2. Create modules (they subscribe to eventBus in their constructors)
 *  3. Register Fabric event callbacks (tick, HUD, chat, join/leave)
 *  4. Register keybind + client command
 */
public final class ZombieZCompanionClient implements ClientModInitializer {

    public static final String MOD_ID = "zombiezcompanion";

    // Singletons exposed for Mod Menu integration and commands
    private static ConfigManager    configManager;
    private static ModuleRegistry   moduleRegistry;
    private static InternalEventBus eventBus;

    @Override
    public void onInitializeClient() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID);

        configManager  = new ConfigManager(configDir);
        eventBus       = new InternalEventBus();
        moduleRegistry = new ModuleRegistry();

        // ---- Build modules ------------------------------------------------
        SessionHistoryStorage historyStorage = new SessionHistoryStorage(configDir);
        SessionManager sessionManager = new SessionManager(
                configManager.getConfig().sessionTracker, eventBus, historyStorage);

        moduleRegistry.register(new EventAlertsModule(configManager.getConfig().eventAlerts, eventBus));
        moduleRegistry.register(new SmartHudModule(configManager.getConfig().smartHud, eventBus));
        moduleRegistry.register(new SessionTrackerModule(configManager.getConfig().sessionTracker, sessionManager, eventBus));

        for (IModule module : moduleRegistry.getModules()) {
            module.onInitialize(configManager.getConfig());
        }

        // ---- Fabric event hooks ------------------------------------------
        registerFabricEvents();
        registerKeybind();
        registerClientCommand();

        ModLogger.info("ZombieZ Companion initialized (" + moduleRegistry.getModules().size() + " modules).");
    }

    // ---- Fabric events --------------------------------------------------

    private void registerFabricEvents() {

        // Tick
        ClientTickEvents.END_CLIENT_TICK.register(client ->
                moduleRegistry.onTick(client));

        // HUD – note: RenderTickCounter introduced in Fabric API 0.100+
        // If you get a compile error here, check the exact HudRenderCallback signature for your fabric-api version.
        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                moduleRegistry.onHudRender(drawContext, tickCounter.getTickDelta(true)));

        // Player-written chat
        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, timestamp) -> {
            moduleRegistry.onChatMessage(message, false);
            dispatchParsedEvent(message, false);
        });

        // System / game messages (skip action-bar overlay)
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) {
                moduleRegistry.onChatMessage(message, true);
                dispatchParsedEvent(message, true);
            }
        });

        // Join
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) ->
                moduleRegistry.onGameJoin());

        // Disconnect – also save config
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            moduleRegistry.onGameLeave();
            configManager.save();
        });
    }

    /** Parse the raw text and publish a typed internal event on the bus. */
    private static void dispatchParsedEvent(Text message, boolean isSystem) {
        ParsedEventInfo info = ChatMessageParser.parseEvent(TextUtils.plain(message));
        if (info != null) {
            eventBus.publish(new InternalEvent(info.type(), info));
        }

        // Kill detection → SESSION_KILL event
        if (ChatMessageParser.isKillMessage(TextUtils.plain(message))) {
            eventBus.publish(new InternalEvent(ServerEventType.KILL_DETECTED));
        }
    }

    // ---- Keybind --------------------------------------------------------

    private static KeyBinding openMenuKey;

    private void registerKeybind() {
        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.zombiezcompanion.open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Y,
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
                    ModLogger.info("=== ZZC Debug ===");
                    for (IModule m : moduleRegistry.getModules()) {
                        ModLogger.info("  " + m.getId() + " → " + (m.isEnabled() ? "ON" : "OFF"));
                    }
                    ctx.getSource().sendFeedback(
                            Text.literal("[ZZC] Module states printed to log."));
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
