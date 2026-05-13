package com.keoz5.zombiezcompanion.modules.hud;

import com.keoz5.zombiezcompanion.config.SmartHudConfig;
import com.keoz5.zombiezcompanion.util.TimeUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders the Smart HUD overlay at the configured position.
 * Each visible field is a separate line; disabled lines are simply skipped.
 */
public final class HudRenderer {

    private static final int LINE_HEIGHT  = 10;
    private static final int PADDING      = 4;
    private static final int TEXT_COLOR   = 0xFFFFFFFF;
    private static final int LABEL_COLOR  = 0xFFAAAAAA;

    public void render(DrawContext context, HudState state, SmartHudConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();

        List<String> lines = buildLines(state, config);
        if (lines.isEmpty()) return;

        int x = config.x;
        int y = config.y;

        if (config.showBackground) {
            int bgW = lines.stream().mapToInt(client.textRenderer::getWidth).max().orElse(50) + PADDING * 2;
            int bgH = lines.size() * LINE_HEIGHT + PADDING * 2;
            int alpha = (int) (config.backgroundOpacity * 255) & 0xFF;
            context.fill(x - PADDING, y - PADDING, x + bgW, y + bgH, (alpha << 24));
        }

        for (String line : lines) {
            context.drawTextWithShadow(client.textRenderer, line, x, y, TEXT_COLOR);
            y += LINE_HEIGHT;
        }
    }

    private List<String> buildLines(HudState state, SmartHudConfig config) {
        List<String> lines = new ArrayList<>();

        if (config.showZone && state.zone != null) {
            lines.add(label("Zone") + state.zone);
        }
        if (config.showClass && state.playerClass != null) {
            lines.add(label("Classe") + state.playerClass);
        }
        if (config.showMutation) {
            if (state.mutationReady) {
                lines.add(Formatting.GREEN + "✔ " + Formatting.RESET + "Mutation prête !");
            } else if (state.mutationName != null) {
                int pct = (int) (state.mutationPercent * 100);
                lines.add(label("Mutation") + pct + "%");
            }
        }
        if (config.showStreak && state.streak >= 0) {
            lines.add(label("Streak") + state.streak);
        }
        if (config.showActiveEvent && state.activeEvent != null) {
            lines.add(label("Évènement") + state.activeEvent);
        }
        if (config.showSessionTime && state.sessionStartMs > 0) {
            long elapsed = System.currentTimeMillis() - state.sessionStartMs;
            lines.add(label("Session") + TimeUtils.formatDuration(elapsed));
        }

        return lines;
    }

    private String label(String name) {
        return Formatting.GRAY + name + ": " + Formatting.WHITE;
    }
}
