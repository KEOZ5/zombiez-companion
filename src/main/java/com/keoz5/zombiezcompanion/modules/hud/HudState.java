package com.keoz5.zombiezcompanion.modules.hud;

/**
 * Mutable snapshot of ZombieZ game state used by the HUD renderer.
 * Updated on every tick by HudStateService.
 */
public final class HudState {

    public String  zone           = null;
    public String  playerClass    = null;
    public boolean mutationReady  = false;
    public String  mutationName   = null;
    public float   mutationPercent = 0f;
    public int     streak         = -1;
    public String  activeEvent    = null;

    /** Set by SessionTracker via event bus when a session starts. 0 = no active session. */
    public long    sessionStartMs = 0;

    /** Timestamp of last full update – use for staleness checks if needed. */
    public long    lastUpdateMs   = 0;

    public void reset() {
        zone           = null;
        playerClass    = null;
        mutationReady  = false;
        mutationName   = null;
        mutationPercent = 0f;
        streak         = -1;
        activeEvent    = null;
        sessionStartMs = 0;
    }
}
