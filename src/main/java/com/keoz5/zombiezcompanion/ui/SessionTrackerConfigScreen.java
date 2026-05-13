package com.keoz5.zombiezcompanion.ui;

import com.keoz5.zombiezcompanion.config.ConfigManager;
import com.keoz5.zombiezcompanion.config.SessionTrackerConfig;
import com.keoz5.zombiezcompanion.modules.tracker.SessionManager;
import com.keoz5.zombiezcompanion.modules.tracker.SessionStats;
import com.keoz5.zombiezcompanion.ui.widgets.ToggleButtonWidget;
import com.keoz5.zombiezcompanion.util.TimeUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public final class SessionTrackerConfigScreen extends Screen {

    private final Screen                parent;
    private final ConfigManager         configManager;
    private final SessionTrackerConfig  config;
    @Nullable private final SessionManager sessionManager;

    public SessionTrackerConfigScreen(Screen parent, ConfigManager configManager,
                                      @Nullable SessionManager sessionManager) {
        super(Text.literal("Session Tracker – Settings"));
        this.parent          = parent;
        this.configManager   = configManager;
        this.config          = configManager.getConfig().sessionTracker;
        this.sessionManager  = sessionManager;
    }

    @Override
    protected void init() {
        int cx   = width / 2;
        int y    = 50;
        int rowH = 24;

        addToggleRow(cx, y, "Démarrage auto à la connexion", config.autoStartOnJoin,  v -> config.autoStartOnJoin  = v); y += rowH;
        addToggleRow(cx, y, "Sauvegarder l'historique",      config.persistHistory,   v -> config.persistHistory   = v); y += rowH;
        addToggleRow(cx, y, "Suivre les kills",              config.trackKills,       v -> config.trackKills       = v); y += rowH;
        addToggleRow(cx, y, "Suivre le streak",              config.trackStreak,      v -> config.trackStreak      = v); y += rowH;
        addToggleRow(cx, y, "Suivre les events",             config.trackEvents,      v -> config.trackEvents      = v); y += rowH;
        addToggleRow(cx, y, "Suivre le loot",                config.trackLoot,        v -> config.trackLoot        = v); y += rowH;

        // Manual session controls
        y += 6;
        addDrawableChild(ButtonWidget.builder(Text.literal("Reset Session"), btn -> {
            if (sessionManager != null) sessionManager.resetSession();
        }).dimensions(cx - 155, y, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Terminer Session"), btn -> {
            if (sessionManager != null) sessionManager.endSession();
        }).dimensions(cx - 30, y, 120, 20).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Sauvegarder & Retour"), btn -> close())
                .dimensions(cx - 75, height - 32, 150, 20).build());
    }

    private void addToggleRow(int cx, int y, String label, boolean initial,
                               java.util.function.Consumer<Boolean> setter) {
        addDrawableChild(new ToggleButtonWidget(cx - 155, y, 50, 20, initial, v -> {
            setter.accept(v);
            configManager.save();
        }));
        addDrawableChild(ButtonWidget.builder(
                Text.literal(label).formatted(Formatting.WHITE), btn -> {}
        ).dimensions(cx - 100, y, 255, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);

        // Current session summary
        if (sessionManager != null && sessionManager.isActive()) {
            SessionStats stats = sessionManager.getCurrentStats();
            assert stats != null;
            String summary = String.format("Session: %s  |  Kills: %d  |  Streak max: %d  |  Events: %d",
                    TimeUtils.formatDuration(stats.getDurationMs()),
                    stats.kills, stats.maxStreak, stats.eventsCompleted);
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal(summary).formatted(Formatting.AQUA),
                    width / 2, height - 52, 0xFFFFFF);
        } else {
            context.drawCenteredTextWithShadow(textRenderer,
                    Text.literal("Aucune session active").formatted(Formatting.GRAY),
                    width / 2, height - 52, 0xFFFFFF);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        configManager.save();
        client.setScreen(parent);
    }
}
