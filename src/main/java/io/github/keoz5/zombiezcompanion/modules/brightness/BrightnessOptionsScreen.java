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
 * <p>Slider edits apply live so the user can dial in the right value while
 * watching the world dim or brighten in real time (provided the module is
 * enabled). The persisted config is saved on screen close — mirroring the
 * rest of the UI to avoid hundreds of fsync calls during a drag.
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
        int sliderY = contentY1 + 64;

        addDrawableChild(new StyledSlider(
                sliderX, sliderY, sliderW, sliderH,
                moduleRef.config().gamma,
                BrightnessModule.GAMMA_MIN, BrightnessModule.GAMMA_MAX,
                moduleRef::setGamma,
                v -> Text.literal("Brightness: " + percent(v))));
    }

    private static String percent(double v) {
        return String.format(Locale.ROOT, "%d%%", Math.round(v * 100));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        super.render(ctx, mouseX, mouseY, delta);

        int cx = (panelX1 + panelX2) / 2;
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("Live override of the vanilla gamma slider."),
                cx, contentY1 + 22, Theme.TEXT_PRIMARY);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§80% = Moody  ·  50% = Default  ·  100% = Bright"),
                cx, contentY1 + 36, Theme.TEXT_MUTED);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8Original value is restored when you disable the module."),
                cx, contentY1 + 104, Theme.TEXT_MUTED);
    }
}
