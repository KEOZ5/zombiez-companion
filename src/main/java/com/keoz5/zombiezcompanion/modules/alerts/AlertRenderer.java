package com.keoz5.zombiezcompanion.modules.alerts;

import com.keoz5.zombiezcompanion.config.EventAlertsConfig;
import com.keoz5.zombiezcompanion.util.TimeUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Manages a queue of active alerts and renders them on screen.
 * - Center area: latest active alert (large, prominent)
 * - Top-right corner: small history list
 */
public final class AlertRenderer {

    private record ActiveAlert(DetectedServerEvent event, long expiresAtMs) {}

    private final Deque<ActiveAlert> active  = new ArrayDeque<>();
    private final List<DetectedServerEvent> history = new ArrayList<>();

    // ---- API called by EventAlertsModule --------------------------------

    public void addAlert(DetectedServerEvent event, long durationMs) {
        long now = System.currentTimeMillis();
        active.addFirst(new ActiveAlert(event, now + durationMs));

        history.add(0, event);
        // Keep history bounded
        while (history.size() > 20) history.remove(history.size() - 1);
    }

    public void clear() {
        active.clear();
        history.clear();
    }

    // ---- Render ---------------------------------------------------------

    public void render(DrawContext context, EventAlertsConfig config) {
        long now = System.currentTimeMillis();
        active.removeIf(a -> a.expiresAtMs() < now);

        MinecraftClient client = MinecraftClient.getInstance();
        int screenW = client.getWindow().getScaledWidth();
        int screenH = client.getWindow().getScaledHeight();

        if (config.showCenterAlert && !active.isEmpty()) {
            renderCenterAlert(context, active.peekFirst(), config, screenW, screenH, now);
        }

        if (config.showTopRightHistory) {
            renderHistoryPanel(context, config, screenW, client);
        }
    }

    // ---- Private rendering helpers --------------------------------------

    private void renderCenterAlert(DrawContext ctx, ActiveAlert alert,
                                   EventAlertsConfig config, int sw, int sh, long now) {
        MinecraftClient client = MinecraftClient.getInstance();
        String label = "⚡ " + alert.event().displayName();
        int textW = client.textRenderer.getWidth(label);
        int x = (sw - textW) / 2;
        int y = sh / 4;  // upper-center area

        // Slight background
        ctx.fill(x - 4, y - 3, x + textW + 4, y + 11, 0x88000000);
        ctx.drawTextWithShadow(client.textRenderer, label, x, y, 0xFFFF5555);

        // Timer
        if (config.showTimer && alert.event().durationSeconds() > 0) {
            long elapsed = (now - alert.event().detectedAtMs()) / 1000;
            int remaining = (int) Math.max(0, alert.event().durationSeconds() - elapsed);
            String timerText = TimeUtils.formatCountdown(remaining);
            int timerW = client.textRenderer.getWidth(timerText);
            ctx.drawTextWithShadow(client.textRenderer, timerText,
                    (sw - timerW) / 2, y + 12, 0xFFFFAA00);
        }
    }

    private void renderHistoryPanel(DrawContext ctx, EventAlertsConfig config, int sw,
                                    MinecraftClient client) {
        int limit = Math.min(config.maxHistoryEntries, history.size());
        if (limit == 0) return;

        int lineH   = 10;
        int padding = 4;
        int panelW  = 110;
        int panelX  = sw - panelW - 4;
        int panelY  = 30;

        ctx.fill(panelX - padding, panelY - padding,
                sw - 4 + padding, panelY + limit * lineH + padding,
                0x66000000);

        for (int i = 0; i < limit; i++) {
            DetectedServerEvent e = history.get(i);
            String line = e.displayName();
            // Fade older entries
            int alpha = i == 0 ? 0xFF : (0xAA - i * 0x18);
            int color = (Math.max(alpha, 0x44) << 24) | 0xFFFFFF;
            ctx.drawTextWithShadow(client.textRenderer, line, panelX, panelY + i * lineH, color);
        }
    }
}
