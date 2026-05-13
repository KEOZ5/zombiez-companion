package com.keoz5.zombiezcompanion.ui;

import com.keoz5.zombiezcompanion.config.ConfigManager;
import com.keoz5.zombiezcompanion.config.EventAlertsConfig;
import com.keoz5.zombiezcompanion.ui.widgets.ToggleButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class EventAlertsConfigScreen extends Screen {

    private final Screen        parent;
    private final ConfigManager configManager;
    private final EventAlertsConfig config;

    public EventAlertsConfigScreen(Screen parent, ConfigManager configManager) {
        super(Text.literal("Event Alerts – Settings"));
        this.parent        = parent;
        this.configManager = configManager;
        this.config        = configManager.getConfig().eventAlerts;
    }

    @Override
    protected void init() {
        int cx = width / 2;
        int y  = 50;
        int rowH = 24;

        addToggleRow(cx, y,      "Alerte centrale",       config.showCenterAlert,  v -> config.showCenterAlert  = v); y += rowH;
        addToggleRow(cx, y,      "Historique (haut-droit)", config.showTopRightHistory, v -> config.showTopRightHistory = v); y += rowH;
        addToggleRow(cx, y,      "Son",                   config.playSound,         v -> config.playSound        = v); y += rowH;
        addToggleRow(cx, y,      "Afficher timer",        config.showTimer,         v -> config.showTimer        = v); y += rowH;

        // Duration label (read-only for now, full text-field not implemented)
        // TODO: add TextFieldWidget for alertDurationMs and maxHistoryEntries

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
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        configManager.save();
        client.setScreen(parent);
    }
}
