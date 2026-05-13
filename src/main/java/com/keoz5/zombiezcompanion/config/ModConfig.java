package com.keoz5.zombiezcompanion.config;

/**
 * Root configuration object serialized to/from JSON.
 * Each sub-config has its own defaults defined in its class.
 */
public class ModConfig {

    /**
     * When true, extra logs are printed for scoreboard lines, detected events,
     * and session updates — useful to discover the actual ZombieZ text formats.
     */
    public boolean debugMode = false;

    public EventAlertsConfig    eventAlerts     = new EventAlertsConfig();
    public SmartHudConfig       smartHud        = new SmartHudConfig();
    public SessionTrackerConfig sessionTracker  = new SessionTrackerConfig();
}
