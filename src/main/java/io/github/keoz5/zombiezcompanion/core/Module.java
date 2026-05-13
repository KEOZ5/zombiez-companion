package io.github.keoz5.zombiezcompanion.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Contract for every feature module.
 *
 * <p>A module declares a stable {@link #id() id}, an optional
 * {@link #displayName() label} for the config UI, an optional
 * {@link #category()} for tab placement, and overrides the hooks it cares
 * about. {@link ModuleManager} only dispatches hooks to modules that are
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
 *
 * <p>Optional UI surface — override {@link #hasOptions()} to {@code true} and
 * implement {@link #createOptionsScreen(Screen)} when the module has its own
 * settings page.
 */
public interface Module {

    String id();

    default String displayName() { return id(); }

    default ModuleCategory category() { return ModuleCategory.UTILITY; }

    default boolean defaultEnabled() { return true; }

    /** Whether the OPTIONS button on the module card should be active. */
    default boolean hasOptions() { return false; }

    /**
     * Build the options screen for this module. Return {@code null} when there
     * are no options (the default). The {@code parent} screen should be
     * restored on close — see {@code ui.ModuleOptionsScreen} for a helper base.
     */
    default Screen createOptionsScreen(Screen parent) { return null; }

    default void onRegister(ModuleContext ctx) {}

    default void onEnable() {}

    default void onDisable() {}

    default void onClientTick(MinecraftClient client) {}

    default void onChatMessage(Text message, boolean overlay) {}

    default void onHudRender(DrawContext drawContext, float tickDelta) {}

    default void onJoinWorld() {}

    default void onLeaveWorld() {}
}
