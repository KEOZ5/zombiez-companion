package io.github.keoz5.zombiezcompanion.ui;

import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.Module;
import io.github.keoz5.zombiezcompanion.ui.theme.Theme;
import io.github.keoz5.zombiezcompanion.ui.widget.StyledButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Base class for per-module options screens.
 *
 * <p>Uses the same inset panel chrome as {@link ConfigScreen} so every
 * module's options page feels visually consistent. Subclasses only need to
 * populate widgets in {@link #initOptions()}; the surrounding chrome (panel,
 * title band, footer with Back button) is drawn automatically.
 *
 * <p>Closing the screen always saves the config so option changes persist
 * even if the user hits Escape.
 */
public abstract class ModuleOptionsScreen extends Screen {

    protected final Screen parent;
    protected final Module module;
    protected final ConfigManager configManager;

    protected int panelX1, panelY1, panelX2, panelY2;
    protected int titleY1, titleY2;
    protected int contentY1, contentY2;
    protected int footerY1, footerY2;

    protected ModuleOptionsScreen(Screen parent, Module module, ConfigManager configManager) {
        super(Text.literal(module.displayName()));
        this.parent = parent;
        this.module = module;
        this.configManager = configManager;
    }

    @Override
    protected final void init() {
        computePanelRect();

        int btnH = 20;
        int btnY = footerY1 + (Theme.FOOTER_BAND_H - btnH) / 2;
        addDrawableChild(new StyledButton(
                panelX1 + Theme.PADDING_MD, btnY, 100, btnH,
                Text.literal("< Retour"),
                b -> close(),
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY));

        initOptions();
    }

    private void computePanelRect() {
        int panelW = Math.min(Theme.PANEL_MAX_WIDTH, width - 2 * Theme.PANEL_MARGIN_MIN);
        int panelH = Math.min(Theme.PANEL_MAX_HEIGHT, height - 2 * Theme.PANEL_MARGIN_MIN);
        panelW = Math.max(panelW, 480);
        panelH = Math.max(panelH, 280);
        panelX1 = (width - panelW) / 2;
        panelY1 = (height - panelH) / 2;
        panelX2 = panelX1 + panelW;
        panelY2 = panelY1 + panelH;

        titleY1   = panelY1;
        titleY2   = titleY1 + Theme.TITLE_BAND_H;
        footerY2  = panelY2;
        footerY1  = footerY2 - Theme.FOOTER_BAND_H;
        contentY1 = titleY2;
        contentY2 = footerY1;
    }

    /** Populate option widgets here. The content band is {@code contentY1..contentY2}. */
    protected abstract void initOptions();

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Theme.BG_DIM);
        ctx.fill(panelX1, panelY1, panelX2, panelY2, Theme.BG_PANEL);
        ctx.fill(panelX1, titleY1, panelX2, titleY2, Theme.BG_BAND);
        ctx.fill(panelX1, contentY1, panelX2, contentY2, Theme.BG_CONTENT);
        ctx.fill(panelX1, footerY1, panelX2, footerY2, Theme.BG_BAND);
        ctx.fill(panelX1, titleY2 - 1, panelX2, titleY2, Theme.BORDER);
        ctx.fill(panelX1, footerY1, panelX2, footerY1 + 1, Theme.BORDER);
        ctx.drawBorder(panelX1, panelY1, panelX2 - panelX1, panelY2 - panelY1, Theme.BORDER_STRONG);

        int titleY = titleY1 + (Theme.TITLE_BAND_H - 9) / 2;
        int titleX = panelX1 + Theme.PADDING_MD;
        ctx.drawText(textRenderer, Text.literal(module.displayName()),
                titleX, titleY, Theme.TEXT_PRIMARY, true);
        int nameW = textRenderer.getWidth(module.displayName());
        ctx.drawText(textRenderer,
                Text.literal(module.category().displayName() + " · " + module.id()),
                titleX + nameW + 8, titleY, Theme.TEXT_MUTED, false);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        configManager.save();
        if (client != null) client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }
}
