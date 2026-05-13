package io.github.keoz5.zombiezcompanion.ui;

import io.github.keoz5.zombiezcompanion.ModInfo;
import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.core.Module;
import io.github.keoz5.zombiezcompanion.core.ModuleCategory;
import io.github.keoz5.zombiezcompanion.core.ModuleManager;
import io.github.keoz5.zombiezcompanion.ui.theme.Theme;
import io.github.keoz5.zombiezcompanion.ui.widget.CategoryTabButton;
import io.github.keoz5.zombiezcompanion.ui.widget.StyledButton;
import net.minecraft.client.MinecraftClient;
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
 * <p>Layout (top → bottom):
 * <ol>
 *     <li><b>Header band</b> — mod title + version</li>
 *     <li><b>Tab bar</b> — {@code ALL} plus one tab per {@link ModuleCategory}
 *         present among registered modules, with a right-aligned search field</li>
 *     <li><b>Card grid</b> — responsive column count (1–4) based on screen width.
 *         Each module is rendered as a card with title, category, an OPTIONS
 *         button (greyed if {@link Module#hasOptions()} is false) and a
 *         coloured ENABLED/DISABLED toggle</li>
 *     <li><b>Footer band</b> — global debug toggle + Done</li>
 * </ol>
 *
 * <p>The screen owns no module state; every change is routed through
 * {@link ModuleManager} so config persistence and lifecycle hooks stay
 * authoritative. The search and category filters are local UI state only.
 *
 * <p>Scrolling is not yet implemented (tracked separately). With overflow the
 * extra cards simply render beyond the panel; safe because the panel
 * background fills its own region first.
 */
public final class ConfigScreen extends Screen {

    private final Screen parent;
    private final ConfigManager configManager;
    private final ModuleManager moduleManager;

    private TextFieldWidget searchField;
    private String searchText = "";
    private ModuleCategory selectedCategory = null; // null = ALL

    private int contentTop;
    private int contentBottom;
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

        int barY = Theme.PADDING_LG + 30;

        // ── Tab bar ───────────────────────────────────────────────────────
        int tabsX = Theme.PADDING_LG;
        tabsX += addTab("ALL", null, tabsX, barY) + Theme.TAB_GAP;

        List<ModuleCategory> usedCategories = moduleManager.modules().stream()
                .map(Module::category)
                .distinct()
                .sorted(Comparator.comparing(Enum::ordinal))
                .toList();
        for (ModuleCategory c : usedCategories) {
            tabsX += addTab(c.displayName().toUpperCase(Locale.ROOT), c, tabsX, barY) + Theme.TAB_GAP;
        }

        // ── Search field (right-aligned) ──────────────────────────────────
        int searchX = width - Theme.PADDING_LG - Theme.SEARCH_WIDTH;
        searchField = new TextFieldWidget(
                textRenderer, searchX, barY, Theme.SEARCH_WIDTH, Theme.TAB_HEIGHT,
                Text.literal(""));
        searchField.setMaxLength(64);
        searchField.setPlaceholder(Text.literal("§7Search modules..."));
        searchField.setText(searchText);
        searchField.setChangedListener(s -> {
            searchText = s;
            layoutCards();
        });
        addDrawableChild(searchField);

        // ── Content area ──────────────────────────────────────────────────
        contentTop = Theme.HEADER_HEIGHT + Theme.PADDING_LG;
        contentBottom = height - Theme.FOOTER_HEIGHT - Theme.PADDING_SM;

        int usableWidth = width - 2 * Theme.PADDING_LG;
        columns = Math.max(1,
                Math.min(4, (usableWidth + Theme.CARD_GAP) / (Theme.CARD_WIDTH + Theme.CARD_GAP)));
        int totalGridW = columns * Theme.CARD_WIDTH + (columns - 1) * Theme.CARD_GAP;
        gridLeft = (width - totalGridW) / 2;

        layoutCards();

        // ── Footer ────────────────────────────────────────────────────────
        int footerY = height - Theme.FOOTER_HEIGHT + Theme.PADDING_SM;
        addDrawableChild(new StyledButton(
                Theme.PADDING_LG, footerY, 110, 22,
                debugLabel(),
                btn -> {
                    boolean next = !configManager.get().debugMode;
                    configManager.get().debugMode = next;
                    configManager.save();
                    btn.setMessage(debugLabel());
                },
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY));

        addDrawableChild(new StyledButton(
                width - Theme.PADDING_LG - 110, footerY, 110, 22,
                Text.translatable("gui.done"),
                btn -> close(),
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY));
    }

    private Text debugLabel() {
        return Text.literal("Debug: " + (configManager.get().debugMode ? "§aON" : "§cOFF") + "§r");
    }

    /** Returns the rendered tab width. */
    private int addTab(String label, ModuleCategory cat, int x, int y) {
        int w = Math.max(48, textRenderer.getWidth(label) + 16);
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
        for (int i = 0; i < filtered.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            int x = gridLeft + col * (Theme.CARD_WIDTH + Theme.CARD_GAP);
            int y = contentTop + row * (Theme.CARD_HEIGHT + Theme.CARD_GAP);
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
        int optionsY = y + Theme.CARD_HEIGHT - 44;
        int toggleY  = y + Theme.CARD_HEIGHT - 22;

        StyledButton options = new StyledButton(
                btnX, optionsY, btnW, 18,
                Text.literal("OPTIONS"),
                btn -> openOptions(m),
                Theme.BG_BTN, Theme.BG_BTN_HOVER, Theme.TEXT_PRIMARY);
        options.active = m.hasOptions();
        addDrawableChild(options);

        boolean enabled = moduleManager.isEnabled(m.id());
        StyledButton toggle = new StyledButton(
                btnX, toggleY, btnW, 18,
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
        // Backdrop
        ctx.fill(0, 0, width, height, Theme.BG_DIM);
        ctx.fill(0, 0, width, Theme.HEADER_HEIGHT, Theme.BG_HEADER);
        ctx.fill(0, Theme.HEADER_HEIGHT, width, height - Theme.FOOTER_HEIGHT, Theme.BG_PANEL);
        ctx.fill(0, height - Theme.FOOTER_HEIGHT, width, height, Theme.BG_HEADER);
        ctx.fill(0, Theme.HEADER_HEIGHT - 1, width, Theme.HEADER_HEIGHT, Theme.BORDER);
        ctx.fill(0, height - Theme.FOOTER_HEIGHT, width, height - Theme.FOOTER_HEIGHT + 1, Theme.BORDER);

        // Title + version
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal(ModInfo.MOD_NAME),
                width / 2, Theme.PADDING_LG, Theme.TEXT_PRIMARY);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§7v" + currentVersion()),
                width / 2, Theme.PADDING_LG + 12, Theme.TEXT_MUTED);

        // Card backgrounds (drawn before widgets so buttons render on top)
        for (CardLayout c : cards) drawCardBackground(ctx, c, mouseX, mouseY);

        if (cards.isEmpty()) {
            String msg = moduleManager.modules().isEmpty()
                    ? "No modules registered yet."
                    : "No modules match this filter.";
            ctx.drawCenteredTextWithShadow(textRenderer, Text.literal("§7" + msg),
                    width / 2, (contentTop + contentBottom) / 2, Theme.TEXT_MUTED);
        }

        super.render(ctx, mouseX, mouseY, delta);
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
                x1 + Theme.CARD_WIDTH / 2, y1 + 10, Theme.TEXT_PRIMARY);
        ctx.drawCenteredTextWithShadow(textRenderer,
                Text.literal("§8" + c.module.category().displayName()),
                x1 + Theme.CARD_WIDTH / 2, y1 + 24, Theme.TEXT_MUTED);
    }

    private static String currentVersion() {
        // Fabric loader metadata is the source of truth; fall back to a hard-coded
        // string when the metadata isn't available (dev hot-swap, missing manifest).
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

    // Helper to expose Screen.remove which is protected — we are inside the
    // package boundary here, so no special accessor is needed.

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
