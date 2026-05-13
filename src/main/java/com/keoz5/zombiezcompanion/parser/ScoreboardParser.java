package com.keoz5.zombiezcompanion.parser;

import com.keoz5.zombiezcompanion.util.DebugLogger;
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
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  ALL PATTERNS ARE PROVISIONAL — must be calibrated on the real server  ║
 * ║  See CLAUDE.md §"Procédure de collecte des données in-game"            ║
 * ║  Enable debugMode → join ZombieZ → filter log on [ZombieZ][DEBUG][Scoreboard]  ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public final class ScoreboardParser {

    private ScoreboardParser() {}

    // ── Pattern registry ─────────────────────────────────────────────────

    private record LinePattern(Pattern regex, String fieldName) {}

    /**
     * Zone line.
     * TODO[DATA-NEEDED] Replace regex with exact ZombieZ sidebar zone line format.
     * Calibration: enable debugMode, join ZombieZ, copy clean= lines, look for
     * the line that shows the current area/sector/zone.
     */
    private static final LinePattern ZONE_PATTERN = new LinePattern(
            Pattern.compile("(?i)^(?:zone|secteur|area|carte)\\s*[:\\-]?\\s*(.+)$"),
            "zone"
    );

    /**
     * Class line.
     * TODO[DATA-NEEDED] Replace regex with exact ZombieZ class line format.
     * Calibration: look for the line showing your current in-game class/role.
     */
    private static final LinePattern CLASS_PATTERN = new LinePattern(
            Pattern.compile("(?i)^(?:classe?|class|r[oô]le|role)\\s*[:\\-]?\\s*(.+)$"),
            "class"
    );

    /**
     * Streak line.
     * TODO[DATA-NEEDED] Replace regex with exact ZombieZ streak line format.
     * Calibration: get a kill streak, look for the line that changes with your kills.
     */
    private static final Pattern STREAK_PATTERN =
            Pattern.compile("(?i)(?:streak|s[eé]rie|kill.?streak|combo)\\s*[:\\-]?\\s*(\\d+)");

    // ── Public API ───────────────────────────────────────────────────────

    public static @Nullable ParsedZoneInfo parseZone(Scoreboard scoreboard) {
        return getLines(scoreboard).stream()
                .map(line -> tryMatch(line, ZONE_PATTERN.regex()))
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .map(ParsedZoneInfo::new)
                .orElse(null);
    }

    public static @Nullable ParsedClassInfo parseClass(Scoreboard scoreboard) {
        return getLines(scoreboard).stream()
                .map(line -> tryMatch(line, CLASS_PATTERN.regex()))
                .filter(v -> v != null && !v.isBlank())
                .findFirst()
                .map(ParsedClassInfo::new)
                .orElse(null);
    }

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
     * Logs all sidebar lines in [ZombieZ][DEBUG][Scoreboard] format.
     * Called every ~5 s from ZombieZCompanionClient when debugMode is on.
     *
     * Output format (copy these lines and send them for pattern calibration):
     *   [ZombieZ][DEBUG][Scoreboard] title="<objective name>"
     *   [ZombieZ][DEBUG][Scoreboard] clean="<line text>" score=<value>
     */
    public static void debugPrintSidebar(Scoreboard scoreboard) {
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) {
            DebugLogger.scoreboard("(no sidebar objective active)");
            return;
        }

        String title = strip(sidebar.getDisplayName().getString());
        DebugLogger.scoreboard("title=\"" + title + "\"");

        getSortedEntries(scoreboard, sidebar).forEach(entry ->
            DebugLogger.scoreboard("clean=\"" + strip(entry.owner()) + "\" score=" + entry.value())
        );
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static List<String> getLines(Scoreboard scoreboard) {
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) return List.of();
        return getSortedEntries(scoreboard, sidebar)
                .map(e -> strip(e.owner()))
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }

    private static java.util.stream.Stream<ScoreboardEntry> getSortedEntries(
            Scoreboard scoreboard, ScoreboardObjective objective) {
        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(objective);
        return entries.stream()
                .sorted((a, b) -> Integer.compare(b.value(), a.value()));
    }

    private static @Nullable String tryMatch(String line, Pattern pattern) {
        Matcher m = pattern.matcher(line);
        return (m.find() && m.groupCount() >= 1) ? m.group(1).trim() : null;
    }

    static String strip(String text) {
        return text == null ? "" : text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
