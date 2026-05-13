package io.github.keoz5.zombiezcompanion.command;

import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.Module;
import io.github.keoz5.zombiezcompanion.core.ModuleManager;
import io.github.keoz5.zombiezcompanion.log.Log;
import io.github.keoz5.zombiezcompanion.ui.ConfigScreen;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

/**
 * Registers the {@code /zzc ...} client command tree.
 *
 * <p>Subcommands:
 * <ul>
 *     <li>{@code /zzc menu} — open the config screen</li>
 *     <li>{@code /zzc debug} — toggle global debug mode (persisted)</li>
 *     <li>{@code /zzc status} — list modules with their enable state</li>
 *     <li>{@code /zzc reload} — reload config from disk</li>
 * </ul>
 */
public final class Commands {

    private Commands() {}

    public static void register(ConfigManager configManager, ModuleManager moduleManager) {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) ->
                dispatcher.register(ClientCommandManager.literal("zzc")
                        .then(ClientCommandManager.literal("menu").executes(ctx -> {
                            MinecraftClient.getInstance().setScreen(new ConfigScreen(null, configManager, moduleManager));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("debug").executes(ctx -> {
                            boolean next = !configManager.get().debugMode;
                            configManager.get().debugMode = next;
                            configManager.save();
                            ctx.getSource().sendFeedback(Text.literal(
                                    "[ZZC] Debug mode " + (next ? "§aON" : "§cOFF") + "§r"));
                            Log.info("Debug mode " + (next ? "ON" : "OFF"));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("status").executes(ctx -> {
                            StringBuilder sb = new StringBuilder("[ZZC] Modules:\n");
                            for (Module m : moduleManager.modules()) {
                                sb.append("  • ").append(m.id())
                                        .append(": ")
                                        .append(moduleManager.isEnabled(m.id()) ? "§aON" : "§cOFF")
                                        .append("§r\n");
                            }
                            sb.append("Debug: ").append(configManager.get().debugMode ? "§aON" : "§cOFF");
                            ctx.getSource().sendFeedback(Text.literal(sb.toString()));
                            return 1;
                        }))
                        .then(ClientCommandManager.literal("reload").executes(ctx -> {
                            configManager.save();
                            ctx.getSource().sendFeedback(Text.literal("[ZZC] Config saved"));
                            return 1;
                        }))
                )
        );
    }
}
