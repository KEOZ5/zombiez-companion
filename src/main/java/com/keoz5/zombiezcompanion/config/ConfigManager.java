package com.keoz5.zombiezcompanion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.keoz5.zombiezcompanion.util.ModLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads and saves the main mod config as a JSON file.
 * On any read/write error the mod falls back to defaults rather than crashing.
 */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configFile;
    private ModConfig config;

    public ConfigManager(Path configDir) {
        this.configFile = configDir.resolve("config.json");
        this.config = load();
    }

    private ModConfig load() {
        if (!Files.exists(configFile)) {
            return new ModConfig();
        }
        try {
            String json = Files.readString(configFile);
            ModConfig loaded = GSON.fromJson(json, ModConfig.class);
            // Gson returns null if the file is empty / not a JSON object
            return loaded != null ? fixNulls(loaded) : new ModConfig();
        } catch (Exception e) {
            ModLogger.warn("Failed to load config, using defaults: " + e.getMessage());
            return new ModConfig();
        }
    }

    /** If Gson leaves a sub-config null (e.g. new field in an old JSON), replace with defaults. */
    private ModConfig fixNulls(ModConfig c) {
        if (c.eventAlerts    == null) c.eventAlerts    = new EventAlertsConfig();
        if (c.smartHud       == null) c.smartHud       = new SmartHudConfig();
        if (c.sessionTracker == null) c.sessionTracker = new SessionTrackerConfig();
        return c;
    }

    public void save() {
        try {
            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, GSON.toJson(config));
        } catch (IOException e) {
            ModLogger.error("Failed to save config: " + e.getMessage());
        }
    }

    public ModConfig getConfig() {
        return config;
    }
}
