package io.github.keoz5.zombiezcompanion.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Root configuration POJO serialized to {@code config/zombiezcompanion/config.json}.
 *
 * <p>Top-level fields are global; per-module settings live in
 * {@link #moduleEnabled} and (later) in dedicated per-module sub-objects added
 * by each module via {@code modules.<id>} keys.
 *
 * <p>All fields are public to keep Gson serialization trivial. New fields
 * must have safe default initializers — {@link ConfigManager} relies on those
 * to migrate older config files forward.
 */
public final class ModConfig {

    /** Schema version. Bump on breaking changes; handle migration in ConfigManager. */
    public int schemaVersion = 1;

    /** Global debug toggle. Gates every {@code Log.debug(...)} call. */
    public boolean debugMode = false;

    /** Per-module enable flag, keyed by {@code Module.id()}. */
    public Map<String, Boolean> moduleEnabled = new LinkedHashMap<>();
}
