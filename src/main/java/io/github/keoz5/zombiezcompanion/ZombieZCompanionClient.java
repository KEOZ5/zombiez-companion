package io.github.keoz5.zombiezcompanion;

import io.github.keoz5.zombiezcompanion.command.Commands;
import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.ModuleContext;
import io.github.keoz5.zombiezcompanion.core.ModuleManager;
import io.github.keoz5.zombiezcompanion.event.EventBus;
import io.github.keoz5.zombiezcompanion.keybind.Keybinds;
import io.github.keoz5.zombiezcompanion.log.Log;
import io.github.keoz5.zombiezcompanion.ui.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;

import java.nio.file.Path;

/**
 * Client entry-point.
 *
 * <p>Wiring order:
 * <ol>
 *     <li>Resolve config dir, load {@link ConfigManager}.</li>
 *     <li>Bind {@link Log#bindDebugFlag} so debug lines obey the live config.</li>
 *     <li>Instantiate {@link EventBus} and {@link ModuleManager}.</li>
 *     <li>Register modules here (none in the base skeleton).</li>
 *     <li>Wire Fabric callbacks to dispatch into the {@link ModuleManager}.</li>
 *     <li>Register the {@code /zzc} command and the open-menu keybind.</li>
 *     <li>Start enabled modules.</li>
 * </ol>
 */
public final class ZombieZCompanionClient implements ClientModInitializer {

    private static ConfigManager configManager;
    private static ModuleManager moduleManager;
    private static EventBus eventBus;

    @Override
    public void onInitializeClient() {
        Path configDir = FabricLoader.getInstance().getConfigDir().resolve(ModInfo.MOD_ID);
        configManager = new ConfigManager(configDir);
        Log.bindDebugFlag(() -> configManager.get().debugMode);

        eventBus = new EventBus();
        moduleManager = new ModuleManager(configManager, new ModuleContext(configManager, eventBus));

        registerModules(moduleManager);
        registerFabricHooks();
        Commands.register(configManager, moduleManager);
        Keybinds.register(() -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new ConfigScreen(null, configManager, moduleManager));
        });

        moduleManager.startEnabledModules();
        configManager.save();

        Log.info(ModInfo.MOD_NAME + " initialized — "
                + moduleManager.modules().size() + " module(s), debug="
                + configManager.get().debugMode);
    }

    /**
     * Single place to wire up modules.
     *
     * <p>Adding a module = one line here, plus a class implementing
     * {@link io.github.keoz5.zombiezcompanion.core.Module}. The module is then
     * persisted in {@code config.json} under {@code moduleEnabled.<id>}.
     */
    private static void registerModules(ModuleManager mm) {
        // no modules in the base skeleton
    }

    private void registerFabricHooks() {
        ClientTickEvents.END_CLIENT_TICK.register(moduleManager::onClientTick);

        HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
                moduleManager.onHudRender(drawContext, tickCounter.getTickDelta(true)));

        ClientReceiveMessageEvents.CHAT.register((message, signed, sender, params, timestamp) ->
                moduleManager.onChatMessage(message, false));

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) moduleManager.onChatMessage(message, true);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> moduleManager.onJoinWorld());
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            moduleManager.onLeaveWorld();
            configManager.save();
        });
    }

    public static ConfigManager configManager() { return configManager; }
    public static ModuleManager moduleManager() { return moduleManager; }
    public static EventBus eventBus()           { return eventBus; }
}
