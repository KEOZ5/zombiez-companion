package io.github.keoz5.zombiezcompanion.modules.brightness;

/**
 * Shared state queried by the
 * {@link io.github.keoz5.zombiezcompanion.mixin.LightmapTextureManagerMixin
 * lightmap mixin}. Holds the boost target and an on/off flag.
 *
 * <p>Vanilla settings are never written; the mixin substitutes the gamma
 * value at read time, only inside {@code LightmapTextureManager.update}.
 * Toggling the module flips {@link #active}; the next lightmap pass picks up
 * the new behavior — at most one tick of latency (~50 ms).
 *
 * <p>Volatile fields so toggles from the config screen (Render thread) are
 * picked up by the lightmap pass without explicit synchronization.
 */
public final class BrightnessOverride {

    private static volatile boolean active = false;
    private static volatile double target = 15.0;

    private BrightnessOverride() {}

    public static void enable(double targetValue) {
        target = targetValue;
        active = true;
    }

    public static void disable() {
        active = false;
    }

    public static void setTarget(double targetValue) {
        target = targetValue;
    }

    public static boolean isActive() {
        return active;
    }

    public static double target() {
        return target;
    }
}
