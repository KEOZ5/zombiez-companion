package com.keoz5.zombiezcompanion.parser;

import com.keoz5.zombiezcompanion.util.ModLogger;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Reads the sidebar scoreboard and extracts typed game-state objects.
 *
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │  ALL PATTERNS ARE PROVISIONAL — based on common ZombieZ-style servers  │
 * │  Enable debugMode in ModConfig then join ZombieZ to see the real lines  │
 * │  printed in the log, and adjust the regexes accordingly.               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *
 * How to calibrate:
 *  1. Set debugMode = true in config screen or directly in config.json
 *  2. Join the ZombieZ server
 *  3. Open the Minecraft log (.minecraft/logs/latest.log)
 *  4. Search for "[ScoreboardParser]" entries
 *  5. Copy the "clean=" values and adapt the patterns below
 */
public final class ScoreboardParser {

    private ScoreboardParser() {}

    // ---- Pattern registry -----------------------------------------------
    // Each record holds: regex to match, and how to extract the value from it.

    private record LinePattern(Pattern regex, String fieldName) {}

    /**
     * Zone line — matches things like "Zone: Forêt", "Secteur: B3", "Zone - Centre".
     * TODO: replace with exact ZombieZ format once observed in-game.
     */
    private static final LinePattern ZONE_PATTERN = new LinePattern(
            Pattern.compile("(?i)^(?:zone|secteur|area)\\s*[:\\-]?\\s*(.+)$"),
            "zone"
    );

    /**
     * Class line — matches "Classe: Soldat", "Class: Tank", etc.
     * TODO: replace with exact ZombieZ format.
     */
    private static final LinePattern CLASS_PATTERN = new LinePattern(
            Pattern.compile("(?i)^(?:classe?|class)\\s*[:\\-]?\\s*(.+)$"),
            "class"
    );

    /**
     * Streak line — matches "Streak: 5", "Série: 3", "Kill streak: 7".
     * TODO: replace with exact ZombieZ format.
     */
    private static final Pattern STREAK_PATTERN =
            Pattern.compile("(?i)(?:streak|s[eé]rie|kill.?streak)\\s*[:\\-]?\\s*(\\d+)");

    // ---- Public API -----------------------------------------------------

    /** Zone extracted from sidebar, or null. */
    public static @Nullable ParsedZoneInfo parseZone(Scoreboard scoreboard) {
        return getLines(scoreboard).stream()
                .map(line -> tryMatch(line, ZONE_PATTERN.regex()))
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .map(ParsedZoneInfo::new)
                .orElse(null);
    }

    /** Player class extracted from sidebar, or null. */
    public static @Nullable ParsedClassInfo parseClass(Scoreboard scoreboard) {
        return getLines(scoreboard).stream()
                .map(line -> tryMatch(line, CLASS_PATTERN.regex()))
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .map(ParsedClassInfo::new)
                .orElse(null);
    }

    /** Streak count from sidebar, or -1 if not found. */
    public static int parseStreak(Scoreboard scoreboard) {
        for (String line : getLines(scoreboard)) {
            Matcher m = STREAK_PATTERN.matcher(line);
            if (m.find()) {
                try { return Integer.parseInt(m.group(1)); }
                catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    /**
     * Logs all sidebar lines to the Minecraft log.
     * Call this once per tick (or on demand) when debugMode is enabled.
     * These logs are what you need to calibrate the patterns above.
     */
    public static void debugPrintSidebar(Scoreboard scoreboard) {
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) {
            ModLogger.debug("[ScoreboardParser] No sidebar objective active.");
            return;
        }

        String title = stripFormatting(sidebar.getDisplayName().getString());
        ModLogger.debug("[ScoreboardParser] === Sidebar title: \"" + title + "\" ===");

        getSortedEntries(scoreboard, sidebar).forEach(entry -> {
            String raw   = entry.owner();
            String clean = stripFormatting(raw);
            ModLogger.debug("[ScoreboardParser]   score=" + entry.value()
                    + "  raw=\"" + raw + "\""
                    + "  clean=\"" + clean + "\"");
        });
    }

    // ---- Helpers --------------------------------------------------------

    /** Returns all sidebar lines, cleaned of formatting codes, sorted by descending score. */
    private static List<String> getLines(Scoreboard scoreboard) {
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) return List.of();
        return getSortedEntries(scoreboard, sidebar)
                .map(e -> stripFormatting(e.owner()))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private static java.util.stream.Stream<ScoreboardEntry> getSortedEntries(
            Scoreboard scoreboard, ScoreboardObjective objective) {
        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);
        return entries.stream()
                .sorted((a, b) -> Integer.compare(b.value(), a.value()));
    }

    /**
     * Returns group(1) of the first match, or null.
     * Group 1 is expected to contain the value after the label.
     */
    private static @Nullable String tryMatch(String line, Pattern pattern) {
        Matcher m = pattern.matcher(line);
        if (m.find() && m.groupCount() >= 1) return m.group(1).trim();
        return null;
    }

    private static String stripFormatting(String text) {
        if (text == null) return "";
        // Strip Minecraft § color/format codes
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
