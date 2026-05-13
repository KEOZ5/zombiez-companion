package io.github.keoz5.zombiezcompanion.modules.brightness;

/**
 * Shared state queried by the {@link io.github.keoz5.zombiezcompanion.mixin.SimpleOptionMixin
 * SimpleOption mixin} to decide whether to substitute the gamma option's
 * effective value.
 *
 * <p>Vanilla settings are never mutated. The mixin reads {@link #isActive()}
 * and {@link #target()} on every {@code getValue()} call; when inactive the
 * mixin returns immediately and the game behaves exactly as without the mod.
 *
 * <p>Volatile fields so toggles from the config screen (Render thread) are
 * picked up by the lightmap pass without synchronization.
 */
public final class BrightnessOverride {

    private static volatile boolean active = false;
    private static volatile double targetGamma = 1.0;

    private BrightnessOverride() {}

    public static void enable(double gamma) {
        targetGamma = gamma;
        active = true;
    }

    public static void disable() {
        active = false;
    }

    public static void setTarget(double gamma) {
        targetGamma = gamma;
    }

    public static boolean isActive() {
        return active;
    }

    public static double target() {
        return targetGamma;
    }
}
