package com.keoz5.zombiezcompanion.config;

public class EventAlertsConfig {
    public boolean enabled          = true;
    public boolean showCenterAlert  = true;
    public boolean showTopRightHistory = true;
    public boolean playSound        = true;
    public boolean showTimer        = true;
    public long    alertDurationMs  = 5000;
    public int     maxHistoryEntries = 5;
}
