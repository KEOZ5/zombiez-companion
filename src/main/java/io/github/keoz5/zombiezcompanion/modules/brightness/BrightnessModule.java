package io.github.keoz5.zombiezcompanion.modules.brightness;

import io.github.keoz5.zombiezcompanion.config.BrightnessConfig;
import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.Module;
import io.github.keoz5.zombiezcompanion.core.ModuleCategory;
import io.github.keoz5.zombiezcompanion.core.ModuleContext;
import io.github.keoz5.zombiezcompanion.log.Log;
import io.github.keoz5.zombiezcompanion.log.LogCategory;
import net.minecraft.client.gui.screen.Screen;

/**
 * Brightness module — full-bright override of the lightmap gamma.
 *
 * <p>The vanilla gamma {@code SimpleOption} is never written. The
 * {@link io.github.keoz5.zombiezcompanion.mixin.LightmapTextureManagerMixin
 * lightmap mixin} substitutes the gamma value at the only point where it
 * actually matters: inside the lightmap texture update. As a side benefit
 * the boost can exceed the vanilla clamp at 1.0 — at high values the lerp
 * formula saturates every lightmap pixel to white = real full bright (caves
 * lit as daylight), which the vanilla slider physically cannot reach.
 *
 * <p>Three logical values, kept cleanly separated:
 * <ul>
 *     <li><b>configured</b> ({@link BrightnessConfig#gamma}) — the slider value.</li>
 *     <li><b>vanilla</b> — whatever the user set in vanilla Video Settings; never
 *         touched, always queryable via {@code options.getGamma().getValue()}.</li>
 *     <li><b>applied</b> — what the lightmap actually uses; equal to
 *         {@code configured} when the module is on, otherwise to vanilla.</li>
 * </ul>
 *
 * <p>Default state is <b>disabled</b>; installing the mod never silently
 * changes lighting.
 */
public final class BrightnessModule implements Module {

    public static final String ID = "brightness";

    public static final double GAMMA_MIN = 0.0;
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
        Log.debug(LogCategory.MODULE, "brightness on, target=" + config().gamma);
    }

    @Override
    public void onDisable() {
        BrightnessOverride.disable();
        Log.debug(LogCategory.MODULE, "brightness off");
    }

    /** Called by the options screen on every slider tick. */
    public void setGamma(double newGamma) {
        double clamped = clamp(newGamma);
        config().gamma = clamped;
        if (BrightnessOverride.isActive()) {
            BrightnessOverride.setTarget(clamped);
        }
    }

    @Override
    public Screen createOptionsScreen(Screen parent) {
        return new BrightnessOptionsScreen(parent, this, configManager);
    }

    public BrightnessConfig config() {
        return configManager.get().brightness;
    }

    private double clampedGamma() { return clamp(config().gamma); }

    private static double clamp(double v) {
        return Math.max(GAMMA_MIN, Math.min(GAMMA_MAX, v));
    }
}
