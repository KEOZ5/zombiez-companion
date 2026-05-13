package com.keoz5.zombiezcompanion.modules.tracker;

import java.util.regex.Pattern;

/**
 * A single chat-message pattern used to detect loot or currency gains.
 *
 * @param pattern     compiled regex tested against the stripped chat line
 * @param displayName short label logged/stored when matched
 * @param rare        true for items considered noteworthy (legendary, epic, crate…)
 *
 * ┌──────────────────────────────────────────────────────────────────────┐
 * │  Patterns are PROVISIONAL. Enable debugMode and play on ZombieZ to  │
 * │  observe real reward messages, then update the list in              │
 * │  SessionTrackerModule.LOOT_PATTERNS.                                │
 * └──────────────────────────────────────────────────────────────────────┘
 */
public record LootPattern(Pattern pattern, String displayName, boolean rare) {

    /** Convenience factory. */
    public static LootPattern of(String regex, String label, boolean rare) {
        return new LootPattern(Pattern.compile(regex, Pattern.CASE_INSENSITIVE), label, rare);
    }
}
