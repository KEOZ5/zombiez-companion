package com.keoz5.zombiezcompanion.ui;

import com.keoz5.zombiezcompanion.config.ConfigManager;
import com.keoz5.zombiezcompanion.config.SmartHudConfig;
import com.keoz5.zombiezcompanion.modules.hud.SmartHudModule;
import com.keoz5.zombiezcompanion.ui.widgets.ToggleButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class SmartHudConfigScreen extends Screen {

    private final Screen         parent;
    private final ConfigManager  configManager;
    private final SmartHudConfig config;

    private TextFieldWidget xField;
    private TextFieldWidget yField;

    public SmartHudConfigScreen(Screen parent, ConfigManager configManager,
                                @Nullable SmartHudModule module) {
        super(Text.literal("Smart HUD – Settings"));
        this.parent        = parent;
        this.configManager = configManager;
        this.config        = configManager.getConfig().smartHud;
    }

    @Override
    protected void init() {
        int cx   = width / 2;
        int y    = 48;
        int rowH = 24;

        // ---- Position fields (X / Y) ----
        xField = new TextFieldWidget(textRenderer, cx - 155, y, 48, 18, Text.literal("X"));
        xField.setText(String.valueOf(config.x));
        xField.setChangedListener(s -> {
            try { config.x = Integer.parseInt(s.trim()); }
            catch (NumberFormatException ignored) {}
        });
        addDrawableChild(xField);

        yField = new TextFieldWidget(textRenderer, cx - 100, y, 48, 18, Text.literal("Y"));
        yField.setText(String.valueOf(config.y));
        yField.setChangedListener(s -> {
            try { config.y = Integer.parseInt(s.trim()); }
            catch (NumberFormatException ignored) {}
        });
        addDrawableChild(yField);
        y += rowH;

        // ---- Toggle rows ----
        addToggleRow(cx, y, "Fond semi-transparent", config.showBackground,  v -> config.showBackground  = v); y += rowH;
        addToggleRow(cx, y, "Zone",                  config.showZone,        v -> config.showZone        = v); y += rowH;
        addToggleRow(cx, y, "Classe",                config.showClass,       v -> config.showClass       = v); y += rowH;
        addToggleRow(cx, y, "Mutation",              config.showMutation,    v -> config.showMutation    = v); y += rowH;
        addToggleRow(cx, y, "Streak",                config.showStreak,      v -> config.showStreak      = v); y += rowH;
        addToggleRow(cx, y, "Évènement actif",       config.showActiveEvent, v -> config.showActiveEvent = v); y += rowH;
        addToggleRow(cx, y, "Durée de session",      config.showSessionTime, v -> config.showSessionTime = v);

        // ---- Save & back ----
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Sauvegarder & Retour"), btn -> close()
        ).dimensions(cx - 75, height - 32, 150, 20).build());
    }

    private void addToggleRow(int cx, int y, String label, boolean initial,
                              Consumer<Boolean> setter) {
        addDrawableChild(new ToggleButtonWidget(cx - 155, y, 50, 20, initial, v -> {
            setter.accept(v);
            configManager.save();
        }));
        // Use a read-only styled button as a label (no action)
        addDrawableChild(ButtonWidget.builder(
                Text.literal(label).formatted(Formatting.WHITE),
                btn -> {}
        ).dimensions(cx - 100, y, 255, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);

        // Labels for X/Y fields — drawn before super so they appear behind the fields
        int fieldY = 53; // vertically centered in the 18px field
        context.drawTextWithShadow(textRenderer, "X :", xField.getX() - 18, fieldY, 0xAAAAAA);
        context.drawTextWithShadow(textRenderer, "Y :", yField.getX() - 18, fieldY, 0xAAAAAA);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        configManager.save();
        client.setScreen(parent);
    }
}
