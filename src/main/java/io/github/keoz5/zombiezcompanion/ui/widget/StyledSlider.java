package io.github.keoz5.zombiezcompanion.ui.widget;

import io.github.keoz5.zombiezcompanion.ui.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;

/**
 * Themed slider for any numeric option in a module's options screen.
 *
 * <p>Constructed with the real min/max range; the underlying
 * {@link SliderWidget} normalizes to {@code [0, 1]} internally. The
 * {@code labeller} produces the visible label from the real (un-normalized)
 * value, and {@code onChange} is fired with the same.
 */
public final class StyledSlider extends SliderWidget {

    private final double min;
    private final double max;
    private final DoubleConsumer onChange;
    private final DoubleFunction<Text> labeller;

    public StyledSlider(int x, int y, int width, int height,
                        double initial, double min, double max,
                        DoubleConsumer onChange, DoubleFunction<Text> labeller) {
        super(x, y, width, height, Text.empty(), normalize(initial, min, max));
        this.min = min;
        this.max = max;
        this.onChange = onChange;
        this.labeller = labeller;
        updateMessage(); // refresh label now that the labeller is set
    }

    private static double normalize(double v, double mn, double mx) {
        if (mx <= mn) return 0;
        return Math.max(0, Math.min(1, (v - mn) / (mx - mn)));
    }

    public double currentValue() {
        return min + (max - min) * this.value;
    }

    @Override
    protected void updateMessage() {
        if (labeller != null) setMessage(labeller.apply(currentValue()));
    }

    @Override
    protected void applyValue() {
        if (onChange != null) onChange.accept(currentValue());
    }

    @Override
    public void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int x1 = getX();
        int y1 = getY();
        int x2 = x1 + getWidth();
        int y2 = y1 + getHeight();

        // Track
        ctx.fill(x1, y1, x2, y2, Theme.BG_INPUT);
        ctx.drawBorder(x1, y1, getWidth(), getHeight(), Theme.BORDER);

        // Progress fill — uses the blue accent so the slider doesn't read as
        // a toggle state. The green state colors are reserved for ENABLED.
        int fillW = (int) Math.round(this.value * (getWidth() - 4));
        if (fillW > 0) {
            ctx.fill(x1 + 2, y1 + 2, x1 + 2 + fillW, y2 - 2, Theme.ACCENT_FILL);
        }

        // Handle
        int handleX = x1 + (int) Math.round(this.value * (getWidth() - 6));
        boolean focused = isHovered() || isFocused();
        ctx.fill(handleX, y1, handleX + 6, y2, focused ? Theme.BG_BTN_HOVER : Theme.BG_BTN);
        ctx.drawBorder(handleX, y1, 6, getHeight(), Theme.BORDER_STRONG);

        // Centered label
        MinecraftClient mc = MinecraftClient.getInstance();
        int textW = mc.textRenderer.getWidth(getMessage());
        int tx = x1 + (getWidth() - textW) / 2;
        int ty = y1 + (getHeight() - 8) / 2;
        ctx.drawText(mc.textRenderer, getMessage(), tx, ty, Theme.TEXT_PRIMARY, false);
    }
}
