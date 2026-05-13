package io.github.keoz5.zombiezcompanion.modules.brightness;

import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.ui.ModuleOptionsScreen;
import io.github.keoz5.zombiezcompanion.ui.theme.Theme;
import io.github.keoz5.zombiezcompanion.ui.widget.StyledSlider;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Options page for {@link BrightnessModule}: a single brightness slider.
 *
 * <p>Slider edits are applied live (the user sees the effect immediately when
 * the module is enabled) but only persisted to disk when the screen closes —
 * mirroring the rest of the UI to avoid hundreds of fsync calls during a drag.
 */
public final class BrightnessOptionsScreen extends ModuleOptionsScreen {

    private final BrightnessModule moduleRef;

    public BrightnessOptionsScreen(Screen parent, BrightnessModule module, ConfigManager configManager) {
        super(parent, module, configManager);
        this.moduleRef = module;
    }

    @Override
    protected void initOptions() {
        int sliderW = Math.min(360, (panelX2 - panelX1) - 4 * Theme.PADDING_LG);
        int sliderH = 22;
        int sliderX = (panelX1 + panelX2) / 2 - sliderW / 2;
        int sliderY = contentY1 + 60;

        addDrawableChild(new StyledSlider(
                sliderX, sliderY, sliderW, sliderH,
                moduleRef.config().gamma,
                BrightnessModule.GAMMA_MIN, BrightnessModule.GAMMA_MAX,
                moduleRef::setGamma,
                v -> Text.literal(String.format(Locale.ROOT, "Brightness: %.1f", v))));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int cx = (panelX1 + panelX2) / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Override the in-game gamma."),
                cx, contentY1 + 20, Theme.TEXT_PRIMARY);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§81.0 = vanilla bright preset · 15.0 ≈ full bright"),
                cx, contentY1 + 34, Theme.TEXT_MUTED);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8Active only while the module is enabled."),
                cx, contentY1 + 100, Theme.TEXT_MUTED);
    }
}
