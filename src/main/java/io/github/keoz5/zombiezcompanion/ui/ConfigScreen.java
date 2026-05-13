package io.github.keoz5.zombiezcompanion.ui;

import io.github.keoz5.zombiezcompanion.ModInfo;
import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.Module;
import io.github.keoz5.zombiezcompanion.core.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Single-screen config UI.
 *
 * <p>Lists every registered module with a toggle button and exposes the global
 * debug toggle. Per-module detail screens are added later as modules grow
 * settings of their own.
 */
public final class ConfigScreen extends Screen {

    private final Screen parent;
    private final ConfigManager configManager;
    private final ModuleManager moduleManager;

    public ConfigScreen(Screen parent, ConfigManager configManager, ModuleManager moduleManager) {
        super(Text.translatable("screen." + ModInfo.MOD_ID + ".config.title"));
        this.parent = parent;
        this.configManager = configManager;
        this.moduleManager = moduleManager;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = 40;

        addDrawableChild(ButtonWidget.builder(debugLabel(), btn -> {
                    boolean next = !configManager.get().debugMode;
                    configManager.get().debugMode = next;
                    configManager.save();
                    btn.setMessage(debugLabel());
                })
                .dimensions(centerX - 100, y, 200, 20)
                .build());
        y += 28;

        for (Module m : moduleManager.modules()) {
            String id = m.id();
            addDrawableChild(ButtonWidget.builder(moduleLabel(m), btn -> {
                        moduleManager.setEnabled(id, !moduleManager.isEnabled(id));
                        btn.setMessage(moduleLabel(m));
                    })
                    .dimensions(centerX - 100, y, 200, 20)
                    .build());
            y += 24;
        }

        addDrawableChild(ButtonWidget.builder(Text.translatable("gui.done"), btn -> close())
                .dimensions(centerX - 100, this.height - 28, 200, 20)
                .build());
    }

    private Text debugLabel() {
        return Text.literal("Debug: " + (configManager.get().debugMode ? "§aON" : "§cOFF") + "§r");
    }

    private Text moduleLabel(Module m) {
        boolean on = moduleManager.isEnabled(m.id());
        return Text.literal(m.displayName() + ": " + (on ? "§aON" : "§cOFF") + "§r");
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 16, 0xFFFFFF);
        if (moduleManager.modules().isEmpty()) {
            ctx.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("§7No modules registered yet"),
                    this.width / 2, this.height / 2, 0xAAAAAA);
        }
    }

    @Override
    public void close() {
        configManager.save();
        if (this.client != null) this.client.setScreen(parent);
    }
}
