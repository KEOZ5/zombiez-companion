package com.keoz5.zombiezcompanion.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.keoz5.zombiezcompanion.modules.tracker.SessionStats;
import com.keoz5.zombiezcompanion.util.ModLogger;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Persists session records as a JSON array in the mod config directory.
 */
public final class SessionHistoryStorage {

    private static final int MAX_SESSIONS = 50;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<SessionRecord>>() {}.getType();

    private final Path historyFile;

    public SessionHistoryStorage(Path configDir) {
        this.historyFile = configDir.resolve("session_history.json");
    }

    public void saveSession(SessionStats stats) {
        List<SessionRecord> history = loadHistory();
        history.add(0, SessionRecord.from(stats));
        while (history.size() > MAX_SESSIONS) history.remove(history.size() - 1);
        write(history);
    }

    public List<SessionRecord> loadHistory() {
        if (!Files.exists(historyFile)) return new ArrayList<>();
        try {
            String json = Files.readString(historyFile);
            List<SessionRecord> list = GSON.fromJson(json, LIST_TYPE);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            ModLogger.warn("Could not read session history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void write(List<SessionRecord> history) {
        try {
            Files.createDirectories(historyFile.getParent());
            Files.writeString(historyFile, GSON.toJson(history));
        } catch (IOException e) {
            ModLogger.error("Could not save session history: " + e.getMessage());
        }
    }
}
