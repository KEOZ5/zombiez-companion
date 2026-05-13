package com.keoz5.zombiezcompanion.config;

/**
 * Root configuration object serialized to/from JSON.
 * Fields are public so Gson can access them directly.
 * Each sub-config has its own defaults defined in its class.
 */
public class ModConfig {
    public EventAlertsConfig   eventAlerts     = new EventAlertsConfig();
    public SmartHudConfig      smartHud        = new SmartHudConfig();
    public SessionTrackerConfig sessionTracker  = new SessionTrackerConfig();
}
