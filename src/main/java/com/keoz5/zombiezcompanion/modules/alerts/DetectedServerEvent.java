package com.keoz5.zombiezcompanion.modules.alerts;

import com.keoz5.zombiezcompanion.events.ServerEventType;

/**
 * An event that was successfully detected and is ready to be displayed.
 *
 * @param type            type for routing
 * @param displayName     short label shown to the player
 * @param rawMessage      original chat/title string
 * @param detectedAtMs    System.currentTimeMillis() when detected
 * @param durationSeconds estimated duration, or -1
 */
public record DetectedServerEvent(
        ServerEventType type,
        String displayName,
        String rawMessage,
        long detectedAtMs,
        int durationSeconds
) {}
