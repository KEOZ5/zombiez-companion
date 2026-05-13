package com.keoz5.zombiezcompanion.ui;

import com.keoz5.zombiezcompanion.config.ConfigManager;
import com.keoz5.zombiezcompanion.modules.IModule;
import com.keoz5.zombiezcompanion.modules.ModuleRegistry;
import com.keoz5.zombiezcompanion.modules.hud.SmartHudModule;
import com.keoz5.zombiezcompanion.modules.tracker.SessionTrackerModule;
import com.keoz5.zombiezcompanion.ui.widgets.ToggleButtonWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

/**
 * Main mod configuration screen.
 * Lists all modules with an ON/OFF toggle and a "Settings" button per module.
 * Open with the keybind (default Y) or /zzc menu.
 */
public final class MainConfigScreen extends Screen {

    private final Screen         parent;
    private final ConfigManager  configManager;
    private final ModuleRegistry moduleRegistry;

    // Keep references to toggle buttons so we can refresh them on re-open
    private final List<ToggleButtonWidget> toggles = new ArrayList<>();

    public MainConfigScreen(Screen parent, ConfigManager configManager, ModuleRegistry moduleRegistry) {
        super(Text.literal("ZombieZ Companion Settings"));
        this.parent         = parent;
        this.configManager  = configManager;
        this.moduleRegistry = moduleRegistry;
    }

    @Override
    protected void init() {
        toggles.clear();

        int startY  = 55;
        int rowH    = 28;
        int centerX = width / 2;

        for (IModule module : moduleRegistry.getModules()) {

            int y = startY;
            startY += rowH;

            // Toggle button (left side)
            ToggleButtonWidget toggle = new ToggleButtonWidget(
                    centerX - 155, y, 50, 20,
                    module.isEnabled(),
                    enabled -> {
                        module.setEnabled(enabled);
                        configManager.save();
                    }
            );
            toggles.add(toggle);
            addDrawableChild(toggle);

            // Module name label + settings button (right side)
            addDrawableChild(ButtonWidget.builder(
                    Text.literal(module.getDisplayName()).formatted(Formatting.WHITE),
                    btn -> {
                        Screen detail = buildDetailScreen(module);
                        if (detail != null) client.setScreen(detail);
                    }
            ).dimensions(centerX - 100, y, 255, 20).build());
        }

        // Back / close button
        addDrawableChild(ButtonWidget.builder(
                Text.translatable("gui.back"),
                btn -> close()
        ).dimensions(centerX - 75, height - 32, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 16, 0xFFFFFF);
        context.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Cliquez sur un module pour ses options").formatted(Formatting.GRAY),
                width / 2, 30, 0xFFFFFF);
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        configManager.save();
        client.setScreen(parent);
    }

    // ---- Detail screen routing ------------------------------------------

    private Screen buildDetailScreen(IModule module) {
        return switch (module.getId()) {
            case "event_alerts"    -> new EventAlertsConfigScreen(this, configManager);
            case "smart_hud"       -> new SmartHudConfigScreen(this, configManager,
                    moduleRegistry.getModule(SmartHudModule.class).orElse(null));
            case "session_tracker" -> new SessionTrackerConfigScreen(this, configManager,
                    moduleRegistry.getModule(SessionTrackerModule.class)
                            .map(SessionTrackerModule::getSessionManager).orElse(null));
            default -> null;
        };
    }
}
