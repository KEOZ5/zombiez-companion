package io.github.keoz5.zombiezcompanion.ui.widget;

import io.github.keoz5.zombiezcompanion.ui.theme.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.function.BooleanSupplier;

/**
 * Pill button used in the category tab bar. Its rendering depends on a live
 * {@code selected} predicate so toggling the active tab does not require
 * recreating widgets.
 */
public final class CategoryTabButton extends ButtonWidget {

    private final BooleanSupplier selected;

    public CategoryTabButton(int x, int y, int width, int height,
                             Text message, PressAction action,
                             BooleanSupplier selected) {
        super(x, y, width, height, message, action, DEFAULT_NARRATION_SUPPLIER);
        this.selected = selected;
    }

    @Override
    protected void renderWidget(DrawContext ctx, int mouseX, int mouseY, float delta) {
        boolean isSelected = selected.getAsBoolean();
        boolean hovered = isHovered() || isFocused();

        int bg;
        int textArgb;
        if (isSelected) {
            bg = hovered ? Theme.BG_TAB_ON_HOV : Theme.BG_TAB_ON;
            textArgb = Theme.TEXT_PRIMARY;
        } else {
            bg = hovered ? Theme.BG_TAB_OFF_HOV : Theme.BG_TAB_OFF;
            textArgb = Theme.TEXT_MUTED;
        }

        int x1 = getX();
        int y1 = getY();
        ctx.fill(x1, y1, x1 + getWidth(), y1 + getHeight(), bg);
        ctx.drawBorder(x1, y1, getWidth(), getHeight(),
                isSelected ? Theme.BORDER_FOCUS : Theme.BORDER);

        MinecraftClient mc = MinecraftClient.getInstance();
        int textWidth = mc.textRenderer.getWidth(getMessage());
        int tx = x1 + (getWidth() - textWidth) / 2;
        int ty = y1 + (getHeight() - 8) / 2;
        ctx.drawText(mc.textRenderer, getMessage(), tx, ty, textArgb, false);
    }
}
