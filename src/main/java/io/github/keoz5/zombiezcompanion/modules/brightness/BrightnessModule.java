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
 * Brightness module — overrides the vanilla gamma slider while enabled.
 *
 * <p>Three distinct values are managed:
 * <ul>
 *     <li><b>configured</b> ({@link BrightnessConfig#gamma}) — what the user
 *         chose in the module's slider.</li>
 *     <li><b>vanilla snapshot</b> ({@link BrightnessConfig#vanillaGammaSnapshot})
 *         — captured the first time the module is enabled; restored on disable.
 *         Persisted so a crash mid-session does not lose the user's original.</li>
 *     <li><b>applied</b> — the live value in {@code SimpleOption}; queried
 *         through {@link BrightnessOverride#readCurrent()}.</li>
 * </ul>
 *
 * <p>State transitions:
 * <pre>
 *   onEnable()           snapshot ← read vanilla (if not yet snapshot'd)
 *                        apply(configured)
 *   onDisable()          apply(snapshot) ; snapshot ← null
 *   setGamma(v) (live)   configured ← v ; if enabled, apply(v)
 * </pre>
 *
 * <p>Default state is <b>disabled</b> so installing the mod never silently
 * changes the user's lighting.
 */
public final class BrightnessModule implements Module {

    public static final String ID = "brightness";

    /** Vanilla SimpleOption clamp — values outside this range are silently clamped by the engine anyway. */
    public static final double GAMMA_MIN = 0.0;
    public static final double GAMMA_MAX = 1.0;

    private ConfigManager configManager;
    private boolean enabled;

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
        BrightnessConfig cfg = config();
        snapshotVanillaIfMissing(cfg);
        if (BrightnessOverride.apply(clamp(cfg.gamma))) {
            Log.debug(LogCategory.MODULE, "brightness applied gamma=" + cfg.gamma);
        }
        enabled = true;
    }

    @Override
    public void onDisable() {
        enabled = false;
        BrightnessConfig cfg = config();
        if (cfg.vanillaGammaSnapshot != null) {
            BrightnessOverride.apply(cfg.vanillaGammaSnapshot);
            Log.debug(LogCategory.MODULE, "brightness restored vanilla=" + cfg.vanillaGammaSnapshot);
            cfg.vanillaGammaSnapshot = null;
            configManager.save();
        }
    }

    /** Called by the options screen on every slider tick. */
    public void setGamma(double newGamma) {
        double clamped = clamp(newGamma);
        config().gamma = clamped;
        if (enabled) {
            BrightnessOverride.apply(clamped);
        }
    }

    @Override
    public Screen createOptionsScreen(Screen parent) {
        return new BrightnessOptionsScreen(parent, this, configManager);
    }

    public BrightnessConfig config() {
        return configManager.get().brightness;
    }

    private void snapshotVanillaIfMissing(BrightnessConfig cfg) {
        if (cfg.vanillaGammaSnapshot != null) return;  // already captured (survives restarts)
        Double current = BrightnessOverride.readCurrent();
        if (current == null) return;
        cfg.vanillaGammaSnapshot = current;
        configManager.save();
        Log.debug(LogCategory.MODULE, "brightness captured vanilla snapshot=" + current);
    }

    private static double clamp(double v) {
        return Math.max(GAMMA_MIN, Math.min(GAMMA_MAX, v));
    }
}
