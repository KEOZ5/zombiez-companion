package com.keoz5.zombiezcompanion.events;

/**
 * All internal event types that modules can publish or subscribe to.
 * Add new types here when adding new detectable server events.
 */
public enum ServerEventType {

    // ---- Events detected from chat / title / bossbar ----
    ZOMBIE_BOMB,
    MUTATION_READY,
    DEFUSED,
    SUCCESS,
    BONUS_ACTIVE,
    GENERIC_SERVER_EVENT,

    // ---- State updates parsed from scoreboard / bossbar ----
    ZONE_CHANGED,
    CLASS_CHANGED,
    STREAK_UPDATED,
    MUTATION_STATE_CHANGED,

    // ---- Session lifecycle ----
    SESSION_STARTED,   // payload: Long (startTimeMs)
    SESSION_ENDED,

    // ---- Kill tracking ----
    KILL_DETECTED
}
