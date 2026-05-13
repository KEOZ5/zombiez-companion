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
 * <p>Subclasses get the mod's panel chrome for free (header + back button) and
 * only need to populate their widgets in {@link #initOptions()}.
 *
 * <p>Closing the screen always saves the config so option changes persist
 * even if the user hits Escape rather than the Back button.
 */
public abstract class ModuleOptionsScreen extends Screen {

    protected final Screen parent;
    protected final Module module;
    protected final ConfigManager configManager;

    protected int contentTop;
    protected int contentBottom;

    protected ModuleOptionsScreen(Screen parent, Module module, ConfigManager configManager) {
        super(Text.literal(module.displayName()));
        this.parent = parent;
        this.module = module;
        this.configManager = configManager;
    }

    @Override
    protected final void init() {
        contentTop = Theme.HEADER_HEIGHT;
        contentBottom = height - Theme.FOOTER_HEIGHT;

        int btnW = 100;
        int btnH = 22;
        addDrawableChild(new StyledButton(
                Theme.PADDING_LG, height - Theme.FOOTER_HEIGHT + Theme.PADDING_SM,
                btnW, btnH,
                Text.literal("< Back"),
                b -> close(),
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY));

        initOptions();
    }

    /** Populate option widgets here. Use {@link #addDrawableChild} like any Screen. */
    protected abstract void initOptions();

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Theme.BG_DIM);
        ctx.fill(0, 0, width, Theme.HEADER_HEIGHT, Theme.BG_HEADER);
        ctx.fill(0, Theme.HEADER_HEIGHT, width, height - Theme.FOOTER_HEIGHT, Theme.BG_PANEL);
        ctx.fill(0, height - Theme.FOOTER_HEIGHT, width, height, Theme.BG_HEADER);
        ctx.fill(0, Theme.HEADER_HEIGHT - 1, width, Theme.HEADER_HEIGHT, Theme.BORDER);
        ctx.fill(0, height - Theme.FOOTER_HEIGHT, width, height - Theme.FOOTER_HEIGHT + 1, Theme.BORDER);

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(module.displayName()),
                width / 2, Theme.PADDING_LG, Theme.TEXT_PRIMARY);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7" + module.category().displayName() + " · " + module.id()),
                width / 2, Theme.PADDING_LG + 14, Theme.TEXT_MUTED);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        configManager.save();
        if (client != null) client.setScreen(parent);
    }
}
