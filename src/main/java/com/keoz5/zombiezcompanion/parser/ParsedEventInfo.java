package com.keoz5.zombiezcompanion.parser;

import com.keoz5.zombiezcompanion.events.ServerEventType;

/**
 * A server-side event detected from a chat message, title, or bossbar.
 *
 * @param type            internal event type
 * @param displayName     human-readable event label
 * @param rawText         original text that triggered the detection
 * @param durationSeconds estimated duration extracted from the message, or -1 if unknown
 */
public record ParsedEventInfo(
        ServerEventType type,
        String displayName,
        String rawText,
        int durationSeconds
) {}
