package com.keoz5.zombiezcompanion.parser;

import com.keoz5.zombiezcompanion.events.ServerEventType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw chat strings into typed ParsedEventInfo objects.
 *
 * To add a new detectable event, simply add an entry to EVENT_PATTERNS.
 * Patterns are tested in order; the first match wins.
 */
public final class ChatMessageParser {

    private ChatMessageParser() {}

    // ---- Pattern registry -----------------------------------------------

    private record EventPattern(Pattern pattern, ServerEventType type, String displayName) {}

    private static final List<EventPattern> EVENT_PATTERNS = List.of(
            new EventPattern(
                    Pattern.compile("(?i)zombie.?bomb|bombe.?zombie"),
                    ServerEventType.ZOMBIE_BOMB,
                    "Zombie Bombe"
            ),
            new EventPattern(
                    Pattern.compile("(?i)mutation.{0,15}pr[eê]te?|ready.{0,10}mutation"),
                    ServerEventType.MUTATION_READY,
                    "Mutation Prête"
            ),
            new EventPattern(
                    Pattern.compile("(?i)d[eé]samor[cç][eé]e?"),
                    ServerEventType.DEFUSED,
                    "Désamorcée"
            ),
            new EventPattern(
                    Pattern.compile("(?i)succ[eè]s|mission.{0,10}r[eé]ussie?"),
                    ServerEventType.SUCCESS,
                    "Succès"
            ),
            new EventPattern(
                    Pattern.compile("(?i)bonus.{0,20}activ[eé]|[eé]v[eé]nement.{0,10}sp[eé]cial"),
                    ServerEventType.BONUS_ACTIVE,
                    "Bonus Activé"
            )
    );

    // ---- Kill detection -------------------------------------------------

    /** Pattern to detect a player kill (adapt to the actual ZombieZ format). */
    private static final Pattern KILL_PATTERN =
            Pattern.compile("(?i)tu as (tu[eé]|élimin[eé])|\\+\\d+\\s*kill");

    // ---- Duration extraction --------------------------------------------

    private static final Pattern DURATION_MINUTES =
            Pattern.compile("(\\d+)\\s*(?:min(?:utes?)?|m\\b)");
    private static final Pattern DURATION_SECONDS =
            Pattern.compile("(\\d+)\\s*(?:sec(?:ondes?)?|s\\b)");
    private static final Pattern DURATION_CLOCK =
            Pattern.compile("(\\d+):(\\d{2})");

    // ---- Public API -----------------------------------------------------

    /**
     * Tries to match the raw chat text against all known event patterns.
     *
     * @return a ParsedEventInfo if a pattern matches, null otherwise
     */
    public static @Nullable ParsedEventInfo parseEvent(String rawText) {
        if (rawText == null || rawText.isBlank()) return null;
        String cleaned = stripFormatting(rawText);

        for (EventPattern ep : EVENT_PATTERNS) {
            if (ep.pattern().matcher(cleaned).find()) {
                int duration = extractDurationSeconds(cleaned);
                return new ParsedEventInfo(ep.type(), ep.displayName(), rawText, duration);
            }
        }
        return null;
    }

    /** Returns true if the message looks like a kill notification. */
    public static boolean isKillMessage(String rawText) {
        if (rawText == null) return false;
        return KILL_PATTERN.matcher(stripFormatting(rawText)).find();
    }

    // ---- Helpers --------------------------------------------------------

    private static int extractDurationSeconds(String text) {
        Matcher m = DURATION_MINUTES.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1)) * 60;

        m = DURATION_SECONDS.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1));

        m = DURATION_CLOCK.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2));

        return -1;
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
