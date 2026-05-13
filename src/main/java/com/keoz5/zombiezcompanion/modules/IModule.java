package com.keoz5.zombiezcompanion.modules;

import com.keoz5.zombiezcompanion.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

/**
 * Common contract for every feature module.
 * All lifecycle methods have no-op defaults so modules only override what they need.
 */
public interface IModule {

    /** Unique machine-readable identifier (snake_case). */
    String getId();

    /** Human-readable display name shown in the config screen. */
    String getDisplayName();

    boolean isEnabled();
    void setEnabled(boolean enabled);

    // ---- Lifecycle hooks ------------------------------------------------

    /** Called once during mod initialization. */
    default void onInitialize(ModConfig config) {}

    /** Called every client tick, only when enabled. */
    default void onTick(MinecraftClient client) {}

    /** Called every frame during HUD rendering, only when enabled. */
    default void onHudRender(DrawContext context, float tickDelta) {}

    /** Called when a chat message arrives (chat or system), only when enabled. */
    default void onChatMessage(Text message, boolean isSystem) {}

    /** Called when the player connects to a world/server. */
    default void onGameJoin() {}

    /** Called when the player disconnects. */
    default void onGameLeave() {}

    /** Called after the config is reloaded (e.g. after screen save). */
    default void onConfigChanged(ModConfig config) {}
}
