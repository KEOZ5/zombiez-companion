package com.keoz5.zombiezcompanion.util;

import com.keoz5.zombiezcompanion.config.ModConfig;

/**
 * Centralized debug logger with a uniform prefix format:
 *
 *   [ZombieZ][DEBUG][Category] key="value" ...
 *
 * All output goes to SLF4J at DEBUG level → .minecraft/logs/latest.log
 *
 * ── How to filter in the log file ────────────────────────────────────────
 *   All debug lines  →  search "[ZombieZ][DEBUG]"
 *   Scoreboard only  →  search "[ZombieZ][DEBUG][Scoreboard]"
 *   Kill messages    →  search "[ZombieZ][DEBUG][Kill]"
 *   Loot messages    →  search "[ZombieZ][DEBUG][Loot]"
 *   Event messages   →  search "[ZombieZ][DEBUG][Event]"
 *   HUD state        →  search "[ZombieZ][DEBUG][HudState]"
 *   Bossbars         →  search "[ZombieZ][DEBUG][Bossbar]"
 *   Raw chat         →  search "[ZombieZ][DEBUG][Chat:Raw]"
 *   Parsed chat      →  search "[ZombieZ][DEBUG][Chat:Parsed]"
 * ─────────────────────────────────────────────────────────────────────────
 *
 * Call DebugLogger.init(config) once at mod startup.
 * Toggle at runtime with /zzc debug or in the config screen.
 */
public final class DebugLogger {

    private static ModConfig config;

    private DebugLogger() {}

    /** Wire the config reference once during mod initialization. */
    public static void init(ModConfig modConfig) {
        config = modConfig;
    }

    /** Returns true when debugMode is active. */
    public static boolean isEnabled() {
        return config != null && config.debugMode;
    }

    // ── Category methods ─────────────────────────────────────────────────

    /** Sidebar scoreboard lines — logged every ~5 s when debug is on. */
    public static void scoreboard(String msg) {
        log("Scoreboard", msg);
    }

    /** Every chat message, raw, before any parsing. */
    public static void chatRaw(String raw) {
        log("Chat:Raw", "message=\"" + raw + "\"");
    }

    /** Chat message after a successful pattern match. */
    public static void chatParsed(String type, String label, String raw) {
        log("Chat:Parsed", "type=" + type + " label=\"" + label + "\" from=\"" + raw + "\"");
    }

    /** Message that triggered the kill detector. */
    public static void kill(String raw) {
        log("Kill", "message=\"" + raw + "\"");
    }

    /** Message that matched a loot pattern. */
    public static void loot(String label, boolean rare, String raw) {
        log("Loot", "label=\"" + label + "\" rare=" + rare + " message=\"" + raw + "\"");
    }

    /** Message that matched an event pattern. */
    public static void event(String type, String label, String raw) {
        log("Event", "type=" + type + " label=\"" + label + "\" message=\"" + raw + "\"");
    }

    /** Current HUD state snapshot — logged every ~5 s when debug is on. */
    public static void hudState(String field, String value) {
        log("HudState", field + "=\"" + (value != null ? value : "null") + "\"");
    }

    /** Active bossbar — logged every ~5 s when debug is on. */
    public static void bossbar(String name, float percent) {
        log("Bossbar", "name=\"" + name + "\" percent=" + String.format("%.0f%%", percent * 100));
    }

    /** No active bossbar visible. */
    public static void noBossbar() {
        log("Bossbar", "(none)");
    }

    // ── Private ──────────────────────────────────────────────────────────

    private static void log(String category, String msg) {
        if (!isEnabled()) return;
        ModLogger.debug("[ZombieZ][DEBUG][" + category + "] " + msg);
    }
}
