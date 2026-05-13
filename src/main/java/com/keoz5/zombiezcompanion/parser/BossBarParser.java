package com.keoz5.zombiezcompanion.parser;

import com.keoz5.zombiezcompanion.mixin.BossBarHudAccessor;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.network.ClientBossBar;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Reads active boss bars and extracts typed game-state objects.
 * Accessing the internal bossbar map requires the BossBarHudAccessor mixin.
 */
public final class BossBarParser {

    private BossBarParser() {}

    public static @Nullable ParsedMutationState parseMutation(BossBarHud bossBarHud) {
        Map<UUID, ClientBossBar> bars = ((BossBarHudAccessor) bossBarHud).getBossBars();
        for (ClientBossBar bar : bars.values()) {
            String name = stripFormatting(bar.getName().getString());
            if (name.toLowerCase().contains("mutation")) {
                boolean ready = name.toLowerCase().contains("prête") || name.toLowerCase().contains("ready");
                return new ParsedMutationState(ready, name, bar.getPercent());
            }
        }
        return null;
    }

    /**
     * Returns the name of the first matching bossbar as a generic active-event string.
     * Useful for detecting ZombieZ event bossbars that don't match mutation patterns.
     */
    public static @Nullable String parseActiveEvent(BossBarHud bossBarHud) {
        Map<UUID, ClientBossBar> bars = ((BossBarHudAccessor) bossBarHud).getBossBars();
        for (ClientBossBar bar : bars.values()) {
            String name = stripFormatting(bar.getName().getString());
            if (!name.isEmpty()) return name;
        }
        return null;
    }

    private static String stripFormatting(String text) {
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }
}
