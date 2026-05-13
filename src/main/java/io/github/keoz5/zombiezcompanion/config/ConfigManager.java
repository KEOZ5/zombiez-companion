package io.github.keoz5.zombiezcompanion.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.github.keoz5.zombiezcompanion.log.Log;
import io.github.keoz5.zombiezcompanion.log.LogCategory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Loads, holds, and persists {@link ModConfig}.
 *
 * <p>Persistence is atomic: writes go to {@code config.json.tmp} then move onto
 * {@code config.json}. A corrupt file is backed up to {@code config.json.bak}
 * and replaced with defaults so the mod always starts in a known state.
 */
public final class ConfigManager {

    private static final String FILE_NAME = "config.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configDir;
    private final Path configFile;
    private ModConfig config;

    public ConfigManager(Path configDir) {
        this.configDir = configDir;
        this.configFile = configDir.resolve(FILE_NAME);
        this.config = load();
    }

    public ModConfig get() {
        return config;
    }

    private ModConfig load() {
        try {
            Files.createDirectories(configDir);
        } catch (IOException e) {
            Log.error("Failed to create config dir " + configDir, e);
        }

        if (!Files.exists(configFile)) {
            Log.info("No config file, creating defaults at " + configFile);
            ModConfig fresh = new ModConfig();
            saveInternal(fresh);
            return fresh;
        }

        try {
            String json = Files.readString(configFile);
            ModConfig parsed = GSON.fromJson(json, ModConfig.class);
            if (parsed == null) {
                throw new JsonSyntaxException("Empty or null config");
            }
            if (parsed.moduleEnabled == null) parsed.moduleEnabled = new java.util.LinkedHashMap<>();
            Log.debug(LogCategory.CONFIG, "loaded schemaVersion=" + parsed.schemaVersion);
            return parsed;
        } catch (IOException | JsonSyntaxException e) {
            Log.error("Failed to read " + configFile + " — restoring defaults", e);
            backupCorruptFile();
            ModConfig fresh = new ModConfig();
            saveInternal(fresh);
            return fresh;
        }
    }

    public void save() {
        saveInternal(config);
    }

    private void saveInternal(ModConfig cfg) {
        try {
            Files.createDirectories(configDir);
            Path tmp = configDir.resolve(FILE_NAME + ".tmp");
            Files.writeString(tmp, GSON.toJson(cfg));
            Files.move(tmp, configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            Log.debug(LogCategory.CONFIG, "saved");
        } catch (IOException e) {
            Log.error("Failed to save config to " + configFile, e);
        }
    }

    private void backupCorruptFile() {
        try {
            Path bak = configDir.resolve(FILE_NAME + ".bak");
            Files.move(configFile, bak, StandardCopyOption.REPLACE_EXISTING);
            Log.warn("Corrupt config moved to " + bak);
        } catch (IOException e) {
            Log.error("Failed to back up corrupt config", e);
        }
    }
}
