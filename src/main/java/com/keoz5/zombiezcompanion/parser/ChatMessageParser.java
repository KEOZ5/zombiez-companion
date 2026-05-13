package com.keoz5.zombiezcompanion.parser;

import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.util.DebugLogger;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw chat/title strings into typed ParsedEventInfo objects.
 *
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  ALL PATTERNS ARE PROVISIONAL — must be calibrated on the real server  ║
 * ║  Enable debugMode → join ZombieZ → filter [ZombieZ][DEBUG][Chat:Raw]  ║
 * ║  Copy relevant lines and send them for calibration.                    ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 *
 * To add a new detectable event:
 *  1. Add a value to ServerEventType
 *  2. Add an EventPattern entry to EVENT_PATTERNS below
 */
public final class ChatMessageParser {

    private ChatMessageParser() {}

    // ── Pattern registry ─────────────────────────────────────────────────

    private record EventPattern(Pattern pattern, ServerEventType type, String displayName) {}

    /**
     * All patterns are tested in order; first match wins.
     * Each TODO[DATA-NEEDED] marks a regex that must be verified against real ZombieZ messages.
     */
    private static final List<EventPattern> EVENT_PATTERNS = List.of(

        // TODO[DATA-NEEDED] Confirm exact ZombieZ "Zombie Bombe" announcement text
        new EventPattern(
            Pattern.compile("(?i)zombie.?bomb|bombe.?zombie"),
            ServerEventType.ZOMBIE_BOMB, "Zombie Bombe"
        ),

        // TODO[DATA-NEEDED] Confirm exact "mutation ready" announcement (chat or title)
        new EventPattern(
            Pattern.compile("(?i)mutation.{0,15}pr[eê]te?|pr[eê]te?.{0,10}mutation|mutation.{0,10}ready"),
            ServerEventType.MUTATION_READY, "Mutation Prête"
        ),

        // TODO[DATA-NEEDED] Confirm exact "defused / désamorcée" message format
        new EventPattern(
            Pattern.compile("(?i)d[eé]samor[cç][eé]e?|defused"),
            ServerEventType.DEFUSED, "Désamorcée"
        ),

        // TODO[DATA-NEEDED] Confirm exact "success / mission réussie" message format
        new EventPattern(
            Pattern.compile("(?i)succ[eè]s|mission.{0,10}r[eé]ussie?|[eé]v[eé]nement.{0,10}r[eé]ussi"),
            ServerEventType.SUCCESS, "Succès"
        ),

        // TODO[DATA-NEEDED] Confirm how bonus/special events are announced
        new EventPattern(
            Pattern.compile("(?i)bonus.{0,20}activ[eé]|[eé]v[eé]nement.{0,10}sp[eé]cial|event.{0,10}activ"),
            ServerEventType.BONUS_ACTIVE, "Bonus Activé"
        )
    );

    // ── Kill detection ───────────────────────────────────────────────────

    /**
     * TODO[DATA-NEEDED] Replace with the exact kill notification message format on ZombieZ.
     * Calibration: kill a zombie → copy the [ZombieZ][DEBUG][Chat:Raw] line that appears.
     */
    private static final Pattern KILL_PATTERN =
            Pattern.compile("(?i)tu as (tu[eé]|[eé]limin[eé])|\\+\\d+\\s*kill|kill\\s*\\+\\s*1",
                    Pattern.CASE_INSENSITIVE);

    // ── Duration extraction ──────────────────────────────────────────────

    private static final Pattern DURATION_MINUTES =
            Pattern.compile("(\\d+)\\s*(?:min(?:utes?)?|m\\b)");
    private static final Pattern DURATION_SECONDS =
            Pattern.compile("(\\d+)\\s*(?:sec(?:ondes?)?|s\\b)");
    private static final Pattern DURATION_CLOCK =
            Pattern.compile("(\\d+):(\\d{2})");

    // ── Public API ───────────────────────────────────────────────────────

    /**
     * Tests the message against all EVENT_PATTERNS.
     *
     * @return ParsedEventInfo if a pattern matches, null otherwise
     */
    public static @Nullable ParsedEventInfo parseEvent(String rawText) {
        if (rawText == null || rawText.isBlank()) return null;
        String clean = strip(rawText);

        for (EventPattern ep : EVENT_PATTERNS) {
            if (ep.pattern().matcher(clean).find()) {
                int duration = extractDurationSeconds(clean);
                ParsedEventInfo info = new ParsedEventInfo(ep.type(), ep.displayName(), rawText, duration);
                DebugLogger.chatParsed(ep.type().name(), ep.displayName(), rawText);
                return info;
            }
        }
        return null;
    }

    /** Returns true if the message looks like a kill notification. */
    public static boolean isKillMessage(String rawText) {
        if (rawText == null) return false;
        boolean match = KILL_PATTERN.matcher(strip(rawText)).find();
        if (match) DebugLogger.kill(rawText);
        return match;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static int extractDurationSeconds(String text) {
        Matcher m = DURATION_MINUTES.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1)) * 60;
        m = DURATION_SECONDS.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1));
        m = DURATION_CLOCK.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1)) * 60 + Integer.parseInt(m.group(2));
        return -1;
    }

    private static String strip(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
