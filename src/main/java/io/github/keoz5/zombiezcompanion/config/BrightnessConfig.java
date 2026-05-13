package io.github.keoz5.zombiezcompanion.config;

/**
 * Per-module config for the Brightness module.
 *
 * <p>Holds the three logically distinct values the module orchestrates:
 * <ul>
 *     <li>{@link #gamma} — the value the user chose in the module's slider.
 *         The "configured" value. Persisted across sessions.</li>
 *     <li>{@link #vanillaGammaSnapshot} — the user's vanilla gamma captured the
 *         first time the module was enabled. Persisted so we can still restore
 *         it after a crash or restart. Cleared on disable once restored.</li>
 *     <li>The "applied" value is whatever currently lives in
 *         {@code MinecraftClient.getInstance().options.getGamma().getValue()} —
 *         never stored here; the SimpleOption is the single source of truth.</li>
 * </ul>
 *
 * <p>Reference pattern for every future module: a tiny public-fields POJO
 * referenced by name from {@link ModConfig}, serialized by Gson, with safe
 * defaults so older config files load without manual migration.
 */
public final class BrightnessConfig {

    /**
     * Configured gamma applied while the module is enabled. Range is the
     * vanilla SimpleOption clamp: {@code [0.0, 1.0]} — 0.0 = "Moody",
     * 0.5 = vanilla default, 1.0 = "Bright". Clamped at the UI level; kept
     * lenient here so a hand-edited config doesn't lose the user's value.
     */
    public double gamma = 1.0;

    /**
     * Snapshot of the user's vanilla gamma captured the first time the module
     * was enabled in this install. Persisted across sessions so a crash mid-
     * session does not lose the original value. Cleared on clean disable.
     * Boxed because {@code null} is the meaningful "no snapshot held" state.
     */
    public Double vanillaGammaSnapshot = null;
}
