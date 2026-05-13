package com.keoz5.zombiezcompanion.parser;

import com.keoz5.zombiezcompanion.mixin.BossBarHudAccessor;
import com.keoz5.zombiezcompanion.util.DebugLogger;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.network.ClientBossBar;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Reads active boss bars and extracts typed game-state objects.
 *
 * ╔══════════════════════════════════════════════════════════════════════════╗
 * ║  PROVISIONAL — mutation/event detection based on keyword assumptions   ║
 * ║  Enable debugMode → join ZombieZ → filter [ZombieZ][DEBUG][Bossbar]   ║
 * ║  Copy the name= lines and send them for calibration.                   ║
 * ╚══════════════════════════════════════════════════════════════════════════╝
 */
public final class BossBarParser {

    private BossBarParser() {}

    /**
     * Attempts to detect a mutation bossbar.
     * TODO[DATA-NEEDED] Verify the exact keyword(s) used in ZombieZ mutation bossbars.
     * Calibration: activate mutation in-game, copy the [ZombieZ][DEBUG][Bossbar] name= line.
     */
    public static @Nullable ParsedMutationState parseMutation(BossBarHud bossBarHud) {
        Map<UUID, ClientBossBar> bars = ((BossBarHudAccessor) bossBarHud).getBossBars();
        for (ClientBossBar bar : bars.values()) {
            String name = strip(bar.getName().getString());
            if (name.toLowerCase().contains("mutation")) {
                // TODO[DATA-NEEDED] Confirm the exact "ready" keyword in ZombieZ
                boolean ready = name.toLowerCase().contains("prête")
                        || name.toLowerCase().contains("prete")
                        || name.toLowerCase().contains("ready");
                return new ParsedMutationState(ready, name, bar.getPercent());
            }
        }
        return null;
    }

    /**
     * Returns the name of the first visible bossbar as a generic active-event string.
     * TODO[DATA-NEEDED] If ZombieZ shows multiple bossbars simultaneously, refine this
     * to pick the most relevant one (e.g. exclude mutation bossbar here).
     */
    public static @Nullable String parseActiveEvent(BossBarHud bossBarHud) {
        Map<UUID, ClientBossBar> bars = ((BossBarHudAccessor) bossBarHud).getBossBars();
        for (ClientBossBar bar : bars.values()) {
            String name = strip(bar.getName().getString());
            if (!name.isEmpty()) return name;
        }
        return null;
    }

    /**
     * Logs all currently visible bossbars.
     * Called every ~5 s from ZombieZCompanionClient when debugMode is on.
     *
     * Output format (copy and send for calibration):
     *   [ZombieZ][DEBUG][Bossbar] name="<text>" percent=<xx%>
     */
    public static void debugPrintBossBars(BossBarHud bossBarHud) {
        Map<UUID, ClientBossBar> bars = ((BossBarHudAccessor) bossBarHud).getBossBars();
        if (bars.isEmpty()) {
            DebugLogger.noBossbar();
            return;
        }
        for (ClientBossBar bar : bars.values()) {
            DebugLogger.bossbar(strip(bar.getName().getString()), bar.getPercent());
        }
    }

    private static String strip(String text) {
        return text == null ? "" : text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
