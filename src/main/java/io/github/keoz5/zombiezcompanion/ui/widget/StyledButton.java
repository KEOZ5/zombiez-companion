package io.github.keoz5.zombiezcompanion.ui.widget;

import io.github.keoz5.zombiezcompanion.ui.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Plain rectangular button styled with the mod's {@link Theme}.
 *
 * <p>Three states are rendered:
 * <ul>
 *     <li><b>idle</b> — {@code bgIdle} fill</li>
 *     <li><b>hover</b> — {@code bgHover} fill</li>
 *     <li><b>disabled</b> — {@code bgIdle} fill with muted text</li>
 * </ul>
 *
 * <p>Use {@link #setColors(int, int)} to recolor the button live (handy for a
 * toggle whose enabled state flips its color).
 */
public class StyledButton extends ButtonWidget {

    private int bgIdle;
    private int bgHover;
    private int textColor;

    public StyledButton(int x, int y, int width, int height,
                        Text message, PressAction action,
                        int bgIdle, int bgHover, int textColor) {
        super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
        this.bgIdle = bgIdle;
        this.bgHover = bgHover;
        this.textColor = textColor;
    }

    public void setColors(int bgIdle, int bgHover) {
        this.bgIdle = bgIdle;
        this.bgHover = bgHover;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        int bg;
        int textArgb;
        if (!this.active) {
            bg = bgIdle;
            textArgb = Theme.TEXT_DISABLED;
        } else if (this.isHovered() || this.isFocused()) {
            bg = bgHover;
            textArgb = textColor;
        } else {
            bg = bgIdle;
            textArgb = textColor;
        }

        int x1 = getX();
        int y1 = getY();
        int x2 = x1 + getWidth();
        int y2 = y1 + getHeight();
        ctx.fill(x1, y1, x2, y2, bg);
        ctx.drawBorder(x1, y1, getWidth(), getHeight(), Theme.BORDER);

        MinecraftClient mc = MinecraftClient.getInstance();
        int textWidth = mc.textRenderer.getWidth(getMessage());
        int textHeight = 8;
        int tx = x1 + (getWidth() - textWidth) / 2;
        int ty = y1 + (getHeight() - textHeight) / 2;
        ctx.drawText(mc.textRenderer, getMessage(), tx, ty, textArgb, false);
    }
}
