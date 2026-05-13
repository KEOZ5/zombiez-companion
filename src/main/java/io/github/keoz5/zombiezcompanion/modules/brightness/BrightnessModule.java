package io.github.keoz5.zombiezcompanion.modules.brightness;

import io.github.keoz5.zombiezcompanion.config.BrightnessConfig;
import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.Module;
import io.github.keoz5.zombiezcompanion.core.ModuleCategory;
import io.github.keoz5.zombiezcompanion.core.ModuleContext;
import net.minecraft.client.gui.screen.Screen;

/**
 * Brightness module — read-only gamma override.
 *
 * <p>The module never mutates the vanilla {@code gamma} option. Instead, the
 * {@link io.github.keoz5.zombiezcompanion.mixin.SimpleOptionMixin SimpleOption
 * mixin} substitutes the value returned by {@code getValue()} on the gamma
 * option only when this module is enabled. Disabling the module reverts the
 * effective gamma to whatever the user set in vanilla settings.
 *
 * <p>Default state is <b>disabled</b> so installing the mod does not change
 * the user's lighting on first launch.
 */
public final class BrightnessModule implements Module {

    public static final String ID = "brightness";

    /** Hard floor — 1.0 matches the vanilla "Bright" preset. */
    public static final double GAMMA_MIN = 1.0;
    /** Hard ceiling — anything beyond is indistinguishable from 15.0. */
    public static final double GAMMA_MAX = 15.0;

    private ConfigManager configManager;

    @Override public String id() { return ID; }
    @Override public String displayName() { return "Brightness"; }
    @Override public ModuleCategory category() { return ModuleCategory.VISUAL; }
    @Override public boolean defaultEnabled() { return false; }
    @Override public boolean hasOptions() { return true; }

    @Override
    public void onRegister(ModuleContext ctx) {
        this.configManager = ctx.configManager();
    }

    @Override
    public void onEnable() {
        BrightnessOverride.enable(clampedGamma());
    }

    @Override
    public void onDisable() {
        BrightnessOverride.disable();
    }

    @Override
    public Screen createOptionsScreen(Screen parent) {
        return new BrightnessOptionsScreen(parent, this, configManager);
    }

    public BrightnessConfig config() {
        return configManager.get().brightness;
    }

    /** Called by the options screen whenever the slider moves. */
    public void setGamma(double newGamma) {
        config().gamma = clampGamma(newGamma);
        if (BrightnessOverride.isActive()) {
            BrightnessOverride.setTarget(config().gamma);
        }
    }

    private double clampedGamma() {
        return clampGamma(config().gamma);
    }

    private static double clampGamma(double v) {
        return Math.max(GAMMA_MIN, Math.min(GAMMA_MAX, v));
    }
}
