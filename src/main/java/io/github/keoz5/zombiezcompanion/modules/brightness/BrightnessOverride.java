package io.github.keoz5.zombiezcompanion.modules.brightness;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

/**
 * Thin wrapper around the vanilla gamma {@link SimpleOption}.
 *
 * <p>Holds no state of its own — the module owns the snapshot + configured
 * values, this class only reads from and writes to the live SimpleOption.
 * Setting the value triggers vanilla's own change callback which marks the
 * lightmap dirty, so the effect is visible on the next render frame.
 */
public final class BrightnessOverride {

    private BrightnessOverride() {}

    /** @return the current value the engine is using for gamma, or {@code null} if the option isn't ready yet. */
    public static Double readCurrent() {
        SimpleOption<Double> opt = gammaOption();
        return opt == null ? null : opt.getValue();
    }

    /** @return true if the value was written; false if the option wasn't available. */
    public static boolean apply(double value) {
        SimpleOption<Double> opt = gammaOption();
        if (opt == null) return false;
        opt.setValue(value);
        return true;
    }

    private static SimpleOption<Double> gammaOption() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return null;
        return mc.options.getGamma();
    }
}
