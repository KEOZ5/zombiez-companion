package com.keoz5.zombiezcompanion.parser;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardEntry;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Reads the sidebar scoreboard and extracts typed game-state objects.
 *
 * NOTE: All parsing is based on the text visible in the sidebar.
 * Patterns below are placeholders — adapt them to the actual ZombieZ scoreboard format.
 */
public final class ScoreboardParser {

    private ScoreboardParser() {}

    public static @Nullable ParsedZoneInfo parseZone(Scoreboard scoreboard) {
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) return null;

        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(sidebar);
        for (ScoreboardEntry entry : entries) {
            String line = stripFormatting(entry.owner());
            // TODO: adapt regex to match the actual zone line in ZombieZ sidebar
            if (line.toLowerCase().contains("zone") || line.toLowerCase().contains("secteur")) {
                String zone = line.replaceAll("(?i)zone\\s*[:\\-]?\\s*", "").trim();
                if (!zone.isEmpty()) return new ParsedZoneInfo(zone);
            }
        }
        return null;
    }

    public static @Nullable ParsedClassInfo parseClass(Scoreboard scoreboard) {
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) return null;

        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(sidebar);
        for (ScoreboardEntry entry : entries) {
            String line = stripFormatting(entry.owner());
            // TODO: adapt to actual class line format
            if (line.toLowerCase().contains("classe") || line.toLowerCase().contains("class")) {
                String cls = line.replaceAll("(?i)classe?\\s*[:\\-]?\\s*", "").trim();
                if (!cls.isEmpty()) return new ParsedClassInfo(cls);
            }
        }
        return null;
    }

    /**
     * Tries to extract a numeric streak value from the sidebar.
     *
     * @return streak count, or -1 if not found
     */
    public static int parseStreak(Scoreboard scoreboard) {
        ScoreboardObjective sidebar = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR);
        if (sidebar == null) return -1;

        Collection<ScoreboardEntry> entries = scoreboard.getScoreboardEntries(sidebar);
        for (ScoreboardEntry entry : entries) {
            String line = stripFormatting(entry.owner());
            if (line.toLowerCase().contains("streak") || line.toLowerCase().contains("s[eé]rie")) {
                try {
                    String digits = line.replaceAll("[^0-9]", "");
                    if (!digits.isEmpty()) return Integer.parseInt(digits);
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
