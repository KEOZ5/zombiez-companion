package com.keoz5.zombiezcompanion.modules.tracker;

import java.util.ArrayList;
import java.util.List;

/**
 * All statistics for one play session.
 * Mutable; updated live by SessionManager.
 */
public final class SessionStats {

    public long   startTimeMs   = 0;
    public long   endTimeMs     = 0;  // 0 while session is active

    public int    kills         = 0;
    public int    currentStreak = 0;
    public int    maxStreak     = 0;
    public int    eventsCompleted = 0;

    public String mainZone      = null;

    public final List<String> detectedLoot  = new ArrayList<>();
    public final List<String> eventHistory  = new ArrayList<>();

    // ---- Helpers --------------------------------------------------------

    public long getDurationMs() {
        long end = endTimeMs > 0 ? endTimeMs : System.currentTimeMillis();
        return end - startTimeMs;
    }

    public void updateStreak(int streak) {
        this.currentStreak = streak;
        if (streak > maxStreak) maxStreak = streak;
    }

    public void addLoot(String item) {
        if (!detectedLoot.contains(item)) detectedLoot.add(item);
    }

    public void addEvent(String name) {
        eventsCompleted++;
        eventHistory.add(0, name);
        if (eventHistory.size() > 50) eventHistory.remove(eventHistory.size() - 1);
    }

    public void reset() {
        startTimeMs = System.currentTimeMillis();
        endTimeMs   = 0;
        kills = 0;
        currentStreak = 0;
        maxStreak = 0;
        eventsCompleted = 0;
        mainZone = null;
        detectedLoot.clear();
        eventHistory.clear();
    }
}
