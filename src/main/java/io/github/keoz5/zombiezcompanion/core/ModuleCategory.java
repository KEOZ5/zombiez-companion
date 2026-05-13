package io.github.keoz5.zombiezcompanion.core;

/**
 * Coarse grouping shown as a category tab in the config UI.
 *
 * <p>A category is purely cosmetic — it controls which tab a module appears
 * under and nothing else. The {@code ConfigScreen} only renders tabs for
 * categories that actually have at least one registered module, so adding a
 * value here does not clutter the UI before a module uses it.
 */
public enum ModuleCategory {
    HUD("HUD"),
    ALERTS("Alertes"),
    TRACKING("Suivi"),
    VISUAL("Visuel"),
    UTILITY("Utilitaire");

    private final String displayName;

    ModuleCategory(String displayName) { this.displayName = displayName; }

    public String displayName() { return displayName; }
}
