package io.github.keoz5.zombiezcompanion.ui;

import io.github.keoz5.zombiezcompanion.ModInfo;
import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.Module;
import io.github.keoz5.zombiezcompanion.core.ModuleCategory;
import io.github.keoz5.zombiezcompanion.core.ModuleManager;
import io.github.keoz5.zombiezcompanion.ui.theme.Theme;
import io.github.keoz5.zombiezcompanion.ui.widget.CategoryTabButton;
import io.github.keoz5.zombiezcompanion.ui.widget.StyledButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Main configuration screen — a single, reusable surface for every module.
 *
 * <p>Renders as an inset panel centered on the screen with four bands:
 * <ol>
 *     <li><b>Title band</b> — mod name + version</li>
 *     <li><b>Toolbar band</b> — category tabs (left), module count + search (right)</li>
 *     <li><b>Content band</b> — responsive 1–4 column card grid</li>
 *     <li><b>Footer band</b> — global debug toggle + Done</li>
 * </ol>
 *
 * <p>The panel rect is clamped between {@link Theme#PANEL_MARGIN_MIN} and
 * {@link Theme#PANEL_MAX_WIDTH}/{@link Theme#PANEL_MAX_HEIGHT} so the UI stays
 * compact on 4K monitors and still leaves breathing room on small windows.
 *
 * <p>The screen owns no module state; every change is routed through
 * {@link ModuleManager} so config persistence and lifecycle hooks stay
 * authoritative. Search and category filters are local UI state only.
 *
 * <p>Scrolling is not yet implemented (tracked as a separate issue). With
 * overflow the extra cards simply spill below the content band; the panel
 * chrome stays correct because it's rendered after the cards.
 */
public final class ConfigScreen extends Screen {

    private final Screen parent;
    private final ConfigManager configManager;
    private final ModuleManager moduleManager;

    private TextFieldWidget searchField;
    private String searchText = "";
    private ModuleCategory selectedCategory = null; // null = ALL

    // Panel rect + bands, computed every init()
    private int panelX1, panelY1, panelX2, panelY2;
    private int titleY1, titleY2;
    private int toolbarY1, toolbarY2;
    private int contentY1, contentY2;
    private int footerY1, footerY2;

    private int gridLeft;
    private int columns;

    private final List<CardLayout> cards = new ArrayList<>();

    public ConfigScreen(Screen parent, ConfigManager configManager, ModuleManager moduleManager) {
        super(Text.literal(ModInfo.MOD_NAME));
        this.parent = parent;
        this.configManager = configManager;
        this.moduleManager = moduleManager;
    }

    @Override
    protected void init() {
        cards.clear();

        computePanelRect();

        // ── Toolbar widgets ──────────────────────────────────────────────
        int tabsX = panelX1 + Theme.PADDING_MD;
        int tabsY = toolbarY1 + (Theme.TOOLBAR_BAND_H - Theme.TAB_HEIGHT) / 2;
        tabsX += addTab("ALL", null, tabsX, tabsY) + Theme.TAB_GAP;

        List<ModuleCategory> usedCategories = moduleManager.modules().stream()
                .map(Module::category)
                .distinct()
                .sorted(Comparator.comparing(Enum::ordinal))
                .toList();
        for (ModuleCategory c : usedCategories) {
            tabsX += addTab(c.displayName().toUpperCase(Locale.ROOT), c, tabsX, tabsY)
                    + Theme.TAB_GAP;
        }

        int searchX = panelX2 - Theme.PADDING_MD - Theme.SEARCH_WIDTH;
        int searchY = toolbarY1 + (Theme.TOOLBAR_BAND_H - Theme.TAB_HEIGHT) / 2;
        searchField = new TextFieldWidget(
                textRenderer, searchX, searchY, Theme.SEARCH_WIDTH, Theme.TAB_HEIGHT,
                Text.literal(""));
        searchField.setMaxLength(64);
        searchField.setPlaceholder(Text.literal("§7Search modules..."));
        searchField.setText(searchText);
        searchField.setChangedListener(s -> {
            searchText = s;
            layoutCards();
        });
        addDrawableChild(searchField);

        // ── Content grid layout (responsive) ─────────────────────────────
        int usableWidth = (panelX2 - panelX1) - 2 * Theme.PADDING_MD;
        columns = Math.max(1,
                Math.min(4, (usableWidth + Theme.CARD_GAP) / (Theme.CARD_WIDTH + Theme.CARD_GAP)));
        int totalGridW = columns * Theme.CARD_WIDTH + (columns - 1) * Theme.CARD_GAP;
        gridLeft = panelX1 + ((panelX2 - panelX1) - totalGridW) / 2;

        layoutCards();

        // ── Footer widgets ───────────────────────────────────────────────
        int btnH = 20;
        int btnY = footerY1 + (Theme.FOOTER_BAND_H - btnH) / 2;
        addDrawableChild(new StyledButton(
                panelX1 + Theme.PADDING_MD, btnY, 100, btnH,
                debugLabel(),
                btn -> {
                    boolean next = !configManager.get().debugMode;
                    configManager.get().debugMode = next;
                    configManager.save();
                    btn.setMessage(debugLabel());
                },
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY));

        addDrawableChild(new StyledButton(
                panelX2 - Theme.PADDING_MD - 100, btnY, 100, btnH,
                Text.translatable("gui.done"),
                btn -> close(),
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY));
    }

    private void computePanelRect() {
        int panelW = Math.min(Theme.PANEL_MAX_WIDTH, width - 2 * Theme.PANEL_MARGIN_MIN);
        int panelH = Math.min(Theme.PANEL_MAX_HEIGHT, height - 2 * Theme.PANEL_MARGIN_MIN);
        panelW = Math.max(panelW, 480);  // keep readable at very small windows
        panelH = Math.max(panelH, 280);
        panelX1 = (width - panelW) / 2;
        panelY1 = (height - panelH) / 2;
        panelX2 = panelX1 + panelW;
        panelY2 = panelY1 + panelH;

        titleY1   = panelY1;
        titleY2   = titleY1 + Theme.TITLE_BAND_H;
        toolbarY1 = titleY2;
        toolbarY2 = toolbarY1 + Theme.TOOLBAR_BAND_H;
        footerY2  = panelY2;
        footerY1  = footerY2 - Theme.FOOTER_BAND_H;
        contentY1 = toolbarY2;
        contentY2 = footerY1;
    }

    private Text debugLabel() {
        return Text.literal("Debug: " + (configManager.get().debugMode ? "§aON" : "§cOFF") + "§r");
    }

    /** Returns the rendered tab width. */
    private int addTab(String label, ModuleCategory cat, int x, int y) {
        int w = Math.max(40, textRenderer.getWidth(label) + 14);
        addDrawableChild(new CategoryTabButton(
                x, y, w, Theme.TAB_HEIGHT,
                Text.literal(label),
                btn -> {
                    selectedCategory = cat;
                    layoutCards();
                },
                () -> Objects.equals(selectedCategory, cat)));
        return w;
    }

    // ── Card grid ────────────────────────────────────────────────────────

    private void layoutCards() {
        for (CardLayout c : cards) {
            this.remove(c.optionsBtn);
            this.remove(c.toggleBtn);
        }
        cards.clear();

        List<Module> filtered = filterModules();
        int gridTop = contentY1 + Theme.PADDING_MD;
        for (int i = 0; i < filtered.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            int x = gridLeft + col * (Theme.CARD_WIDTH + Theme.CARD_GAP);
            int y = gridTop + row * (Theme.CARD_HEIGHT + Theme.CARD_GAP);
            cards.add(buildCard(filtered.get(i), x, y));
        }
    }

    private List<Module> filterModules() {
        String q = searchText.toLowerCase(Locale.ROOT).trim();
        return moduleManager.modules().stream()
                .filter(m -> selectedCategory == null || m.category() == selectedCategory)
                .filter(m -> q.isEmpty()
                        || m.displayName().toLowerCase(Locale.ROOT).contains(q)
                        || m.id().toLowerCase(Locale.ROOT).contains(q)
                        || m.category().displayName().toLowerCase(Locale.ROOT).contains(q))
                .toList();
    }

    private CardLayout buildCard(Module m, int x, int y) {
        int btnW = Theme.CARD_WIDTH - 12;
        int btnX = x + 6;
        int optionsY = y + Theme.CARD_HEIGHT - 38;
        int toggleY  = y + Theme.CARD_HEIGHT - 18;

        StyledButton options = new StyledButton(
                btnX, optionsY, btnW, 16,
                Text.literal("OPTIONS"),
                btn -> openOptions(m),
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY);
        options.active = m.hasOptions();
        addDrawableChild(options);

        boolean enabled = moduleManager.isEnabled(m.id());
        StyledButton toggle = new StyledButton(
                btnX, toggleY, btnW, 16,
                toggleLabel(enabled),
                btn -> toggleModule(m, btn),
                enabled ? Theme.STATE_ON_BG : Theme.STATE_OFF_BG,
                enabled ? Theme.STATE_ON_BG_H : Theme.STATE_OFF_BG_H,
                Theme.TEXT_PRIMARY);
        addDrawableChild(toggle);

        return new CardLayout(m, x, y, options, toggle);
    }

    private static Text toggleLabel(boolean enabled) {
        return Text.literal(enabled ? "ENABLED" : "DISABLED");
    }

    private void toggleModule(Module m, net.minecraft.client.gui.widget.ButtonWidget btn) {
        boolean next = !moduleManager.isEnabled(m.id());
        moduleManager.setEnabled(m.id(), next);
        StyledButton sb = (StyledButton) btn;
        sb.setMessage(toggleLabel(next));
        sb.setColors(
                next ? Theme.STATE_ON_BG : Theme.STATE_OFF_BG,
                next ? Theme.STATE_ON_BG_H : Theme.STATE_OFF_BG_H);
    }

    private void openOptions(Module m) {
        if (!m.hasOptions() || client == null) return;
        Screen opts = m.createOptionsScreen(this);
        if (opts != null) client.setScreen(opts);
    }

    // ── Render ───────────────────────────────────────────────────────────

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, Theme.BG_DIM);

        // Panel body
        ctx.fill(panelX1, panelY1, panelX2, panelY2, Theme.BG_PANEL);

        // Title band
        ctx.fill(panelX1, titleY1, panelX2, titleY2, Theme.BG_BAND);

        // Content band
        ctx.fill(panelX1, contentY1, panelX2, contentY2, Theme.BG_CONTENT);

        // Footer band
        ctx.fill(panelX1, footerY1, panelX2, footerY2, Theme.BG_BAND);

        // Inner dividers
        ctx.fill(panelX1, titleY2 - 1,   panelX2, titleY2,   Theme.BORDER);
        ctx.fill(panelX1, toolbarY2 - 1, panelX2, toolbarY2, Theme.BORDER);
        ctx.fill(panelX1, footerY1,      panelX2, footerY1 + 1, Theme.BORDER);

        // Outer border
        ctx.drawBorder(panelX1, panelY1, panelX2 - panelX1, panelY2 - panelY1, Theme.BORDER_STRONG);

        // Title: name + version inline
        int titleY = titleY1 + (Theme.TITLE_BAND_H - 9) / 2;
        int titleX = panelX1 + Theme.PADDING_MD;
        ctx.drawText(textRenderer, Text.literal(ModInfo.MOD_NAME),
                titleX, titleY, Theme.TEXT_PRIMARY, true);
        int nameW = textRenderer.getWidth(ModInfo.MOD_NAME);
        ctx.drawText(textRenderer, Text.literal("v" + currentVersion()),
                titleX + nameW + 8, titleY, Theme.TEXT_MUTED, false);

        // Module count (right-aligned, before search)
        String count = moduleCountLabel();
        int countW = textRenderer.getWidth(count);
        int countX = searchField.getX() - 8 - countW;
        int countY = searchField.getY() + (Theme.TAB_HEIGHT - 9) / 2;
        ctx.drawText(textRenderer, Text.literal(count), countX, countY, Theme.TEXT_MUTED, false);

        // Card backgrounds
        for (CardLayout c : cards) drawCardBackground(ctx, c, mouseX, mouseY);

        // Empty state
        if (cards.isEmpty()) {
            String primary = moduleManager.modules().isEmpty()
                    ? "No modules registered yet."
                    : "No modules match this filter.";
            String hint = moduleManager.modules().isEmpty()
                    ? "Module cards will appear here as features are added."
                    : "Try clearing the search or selecting another tab.";
            int cx = (panelX1 + panelX2) / 2;
            int cy = (contentY1 + contentY2) / 2 - 6;
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal(primary), cx, cy, Theme.TEXT_MUTED);
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§8" + hint), cx, cy + 12, Theme.TEXT_DISABLED);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    private String moduleCountLabel() {
        int total = moduleManager.modules().size();
        int shown = cards.size();
        if (total == 0) return "0 modules";
        if (shown == total) return total + (total == 1 ? " module" : " modules");
        return shown + " / " + total + " modules";
    }

    private void drawCardBackground(DrawContext ctx, CardLayout c, int mx, int my) {
        int x1 = c.x;
        int y1 = c.y;
        int x2 = x1 + Theme.CARD_WIDTH;
        int y2 = y1 + Theme.CARD_HEIGHT;
        boolean hovered = mx >= x1 && mx < x2 && my >= y1 && my < y2;
        ctx.fill(x1, y1, x2, y2, hovered ? Theme.BG_CARD_HOVER : Theme.BG_CARD);
        ctx.drawBorder(x1, y1, Theme.CARD_WIDTH, Theme.CARD_HEIGHT, Theme.BORDER);

        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(c.module.displayName()),
                x1 + Theme.CARD_WIDTH / 2, y1 + 8, Theme.TEXT_PRIMARY);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8" + c.module.category().displayName()),
                x1 + Theme.CARD_WIDTH / 2, y1 + 22, Theme.TEXT_MUTED);
    }

    private static String currentVersion() {
        try {
            return net.fabricmc.loader.api.FabricLoader.getInstance()
                    .getModContainer(ModInfo.MOD_ID)
                    .map(c -> c.getMetadata().getVersion().getFriendlyString())
                    .orElse("?");
        } catch (Throwable t) {
            return "?";
        }
    }

    @Override
    public void close() {
        configManager.save();
        if (this.client != null) this.client.setScreen(parent);
    }

    @Override
    public boolean shouldPause() { return false; }

    private static final class CardLayout {
        final Module module;
        final int x;
        final int y;
        final StyledButton optionsBtn;
        final StyledButton toggleBtn;

        CardLayout(Module module, int x, int y, StyledButton optionsBtn, StyledButton toggleBtn) {
            this.module = module;
            this.x = x;
            this.y = y;
            this.optionsBtn = optionsBtn;
            this.toggleBtn = toggleBtn;
        }
    }
}
