package io.github.keoz5.zombiezcompanion.config;

/**
 * Per-module config for the Brightness module.
 *
 * <p>Reference pattern for every future module: a tiny public-fields POJO
 * referenced by name from {@link ModConfig}, serialized by Gson, with safe
 * defaults so older config files load without manual migration.
 */
public final class BrightnessConfig {

    /**
     * Effective gamma applied while Brightness is enabled. 1.0 matches the
     * vanilla "Bright" preset; values above 1 push past what the vanilla
     * slider allows. Clamped at the UI level — kept lenient here so a hand-
     * edited config doesn't lose the user's value on load.
     */
    public double gamma = 5.0;
}
