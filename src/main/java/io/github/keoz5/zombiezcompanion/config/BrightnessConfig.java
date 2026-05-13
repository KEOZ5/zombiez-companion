package io.github.keoz5.zombiezcompanion.config;

/**
 * Per-module config for the Brightness module.
 *
 * <p>Single value: the configured gamma boost. The vanilla gamma SimpleOption
 * is never mutated by the mod — the
 * {@link io.github.keoz5.zombiezcompanion.mixin.LightmapTextureManagerMixin
 * lightmap mixin} only substitutes the value at read time, so there is no
 * "vanilla snapshot to restore" to track here.
 *
 * <p>Reference pattern for every future module: a tiny public-fields POJO
 * referenced by name from {@link ModConfig}, serialized by Gson, with safe
 * defaults so older config files load without manual migration.
 */
public final class BrightnessConfig {

    /**
     * Configured gamma applied while the module is enabled. Range is
     * {@code [0.0, 15.0]} — vanilla clamps at 1.0, going beyond produces
     * progressively "fuller" full bright until the lightmap pixels saturate
     * to white (around 5–10 depending on biome). Default 15.0 = full bright
     * everywhere. Clamped at the UI level.
     */
    public double gamma = 15.0;
}
