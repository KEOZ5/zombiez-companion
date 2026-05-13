package com.keoz5.zombiezcompanion.modules.hud;

import com.keoz5.zombiezcompanion.parser.BossBarParser;
import com.keoz5.zombiezcompanion.parser.ParsedClassInfo;
import com.keoz5.zombiezcompanion.parser.ParsedMutationState;
import com.keoz5.zombiezcompanion.parser.ParsedZoneInfo;
import com.keoz5.zombiezcompanion.parser.ScoreboardParser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.scoreboard.Scoreboard;

/**
 * Reads client-visible data every tick and pushes it into HudState.
 * Runs inside SmartHudModule.onTick(), so it only fires when the module is enabled.
 */
public final class HudStateService {

    public void update(MinecraftClient client, HudState state) {
        if (client.world == null) return;

        Scoreboard scoreboard = client.world.getScoreboard();

        ParsedZoneInfo zone = ScoreboardParser.parseZone(scoreboard);
        if (zone != null) state.zone = zone.name();

        ParsedClassInfo cls = ScoreboardParser.parseClass(scoreboard);
        if (cls != null) state.playerClass = cls.className();

        int streak = ScoreboardParser.parseStreak(scoreboard);
        if (streak >= 0) state.streak = streak;

        ParsedMutationState mutation = BossBarParser.parseMutation(client.inGameHud.getBossBarHud());
        if (mutation != null) {
            state.mutationReady   = mutation.ready();
            state.mutationName    = mutation.name();
            state.mutationPercent = mutation.percent();
        } else {
            state.mutationReady  = false;
            state.mutationName   = null;
        }

        String event = BossBarParser.parseActiveEvent(client.inGameHud.getBossBarHud());
        state.activeEvent = event;

        state.lastUpdateMs = System.currentTimeMillis();
    }
}
