package io.github.keoz5.zombiezcompanion.log;

/**
 * Stable identifiers for debug log lines.
 *
 * <p>Each module should reuse an existing category or add a new one here so the
 * full set is discoverable. The {@link #tag()} value is what appears between
 * brackets in the log file and is the string users grep for.
 */
public enum LogCategory {
    CORE("Core"),
    CONFIG("Config"),
    MODULE("Module"),
    EVENT("Event"),
    CHAT("Chat"),
    HUD("Hud");

    private final String tag;

    LogCategory(String tag) { this.tag = tag; }

    public String tag() { return tag; }
}
