package com.keoz5.zombiezcompanion.modules.tracker;

import com.keoz5.zombiezcompanion.config.SessionTrackerConfig;
import com.keoz5.zombiezcompanion.events.InternalEvent;
import com.keoz5.zombiezcompanion.events.InternalEventBus;
import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.storage.SessionHistoryStorage;
import com.keoz5.zombiezcompanion.util.ModLogger;
import org.jetbrains.annotations.Nullable;

/**
 * Manages the lifecycle of a single play session:
 * start → accumulate stats → end → persist.
 */
public final class SessionManager {

    private final SessionTrackerConfig   config;
    private final InternalEventBus       eventBus;
    private final SessionHistoryStorage  storage;

    @Nullable
    private SessionStats currentSession = null;

    public SessionManager(SessionTrackerConfig config,
                          InternalEventBus eventBus,
                          SessionHistoryStorage storage) {
        this.config   = config;
        this.eventBus = eventBus;
        this.storage  = storage;
    }

    // ---- Lifecycle ------------------------------------------------------

    public void startSession() {
        if (currentSession != null) {
            endSession();
        }
        currentSession = new SessionStats();
        currentSession.startTimeMs = System.currentTimeMillis();
        eventBus.publish(new InternalEvent(ServerEventType.SESSION_STARTED, currentSession.startTimeMs));
        ModLogger.info("[SessionTracker] Session started.");
    }

    public void endSession() {
        if (currentSession == null) return;
        currentSession.endTimeMs = System.currentTimeMillis();

        if (config.persistHistory) {
            storage.saveSession(currentSession);
        }

        eventBus.publish(new InternalEvent(ServerEventType.SESSION_ENDED));
        ModLogger.info("[SessionTracker] Session ended. Duration: "
                + currentSession.getDurationMs() / 1000 + "s, kills: " + currentSession.kills);
        currentSession = null;
    }

    public void resetSession() {
        if (currentSession != null) {
            currentSession.reset();
            eventBus.publish(new InternalEvent(ServerEventType.SESSION_STARTED, currentSession.startTimeMs));
        }
    }

    // ---- Stat updates ---------------------------------------------------

    public void onKill() {
        if (currentSession == null || !config.trackKills) return;
        currentSession.kills++;
    }

    public void onStreakUpdate(int streak) {
        if (currentSession == null || !config.trackStreak) return;
        currentSession.updateStreak(streak);
    }

    public void onEventCompleted(String name) {
        if (currentSession == null || !config.trackEvents) return;
        currentSession.addEvent(name);
    }

    public void onLootDetected(String item) {
        if (currentSession == null || !config.trackLoot) return;
        currentSession.addLoot(item);
    }

    public void onZoneChanged(String zone) {
        if (currentSession == null) return;
        currentSession.mainZone = zone;
    }

    // ---- Accessors ------------------------------------------------------

    public boolean isActive() { return currentSession != null; }

    @Nullable
    public SessionStats getCurrentStats() { return currentSession; }
}
