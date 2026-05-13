package io.github.keoz5.zombiezcompanion.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Contract for every feature module.
 *
 * <p>A module declares a stable {@link #id() id}, an optional
 * {@link #displayName() label} for the config UI, and overrides the hooks it
 * cares about. {@link ModuleManager} only dispatches hooks to modules that are
 * currently enabled.
 *
 * <p>Lifecycle:
 * <ol>
 *     <li>{@link #onRegister(ModuleContext)} — once at startup, regardless of enable state.
 *         Use this to subscribe to {@code EventBus} or read config.</li>
 *     <li>{@link #onEnable()} / {@link #onDisable()} — called when the user toggles
 *         the module on/off, and on startup if the module is enabled.</li>
 *     <li>Tick / chat / render / world hooks — only while enabled.</li>
 * </ol>
 */
public interface Module {

    String id();

    default String displayName() { return id(); }

    default boolean defaultEnabled() { return true; }

    default void onRegister(ModuleContext ctx) {}

    default void onEnable() {}

    default void onDisable() {}

    default void onClientTick(MinecraftClient client) {}

    default void onChatMessage(Text message, boolean overlay) {}

    default void onHudRender(DrawContext drawContext, float tickDelta) {}

    default void onJoinWorld() {}

    default void onLeaveWorld() {}
}
