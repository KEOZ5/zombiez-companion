package io.github.keoz5.zombiezcompanion.ui.theme;

/**
 * Single source of truth for the mod's UI palette and sizing.
 *
 * <p>Colors are ARGB ints ({@code 0xAARRGGBB}) as expected by Minecraft's
 * {@code DrawContext}. Sizes are in screen pixels (pre-GUI-scale).
 *
 * <p>The palette is dark and tinted green to match the ZombieZ theme without
 * being overbearing. Hover and selected variants are intentionally subtle so
 * the interface stays readable during gameplay.
 */
public final class Theme {

    private Theme() {}

    // ── Backgrounds ──────────────────────────────────────────────────────
    public static final int BG_DIM          = 0xC8000000;  // overlay behind the panel
    public static final int BG_PANEL        = 0xFF161A14;
    public static final int BG_BAND         = 0xFF11140F;  // title / footer bands
    public static final int BG_CONTENT      = 0xFF181D15;  // central card area
    public static final int BG_CARD         = 0xFF1F2419;
    public static final int BG_CARD_HOVER   = 0xFF272E20;
    public static final int BG_BTN          = 0xFF2A311F;
    public static final int BG_BTN_HOVER    = 0xFF353F26;
    public static final int BG_INPUT        = 0xFF0F120B;
    public static final int BG_TAB_OFF      = 0xFF1F2419;
    public static final int BG_TAB_OFF_HOV  = 0xFF272E20;
    public static final int BG_TAB_ON       = 0xFF3E5D2A;
    public static final int BG_TAB_ON_HOV   = 0xFF497034;

    // ── Borders / dividers ──────────────────────────────────────────────
    public static final int BORDER          = 0xFF2C3322;
    public static final int BORDER_STRONG   = 0xFF3A4530;
    public static final int BORDER_FOCUS    = 0xFF6FA94A;

    // ── Text ─────────────────────────────────────────────────────────────
    public static final int TEXT_PRIMARY    = 0xFFE7EFE0;
    public static final int TEXT_MUTED      = 0xFF7C8474;
    public static final int TEXT_DISABLED   = 0xFF4A5043;
    public static final int TEXT_ACCENT     = 0xFFAFD78F;

    // ── Module enable state ─────────────────────────────────────────────
    public static final int STATE_ON_BG     = 0xFF3F7A2C;
    public static final int STATE_ON_BG_H   = 0xFF4D9136;
    public static final int STATE_OFF_BG    = 0xFF7A2C2C;
    public static final int STATE_OFF_BG_H  = 0xFF913636;

    // ── Layout: paddings ────────────────────────────────────────────────
    public static final int PADDING_LG = 14;
    public static final int PADDING_MD = 10;
    public static final int PADDING_SM = 6;

    // ── Layout: panel chrome ────────────────────────────────────────────
    /** Floor for the visible margin between the panel and the screen edge. */
    public static final int PANEL_MARGIN_MIN = 24;
    /** Hard cap on panel width — beyond this we keep the panel centered. */
    public static final int PANEL_MAX_WIDTH  = 1000;
    /** Hard cap on panel height. */
    public static final int PANEL_MAX_HEIGHT = 620;

    public static final int TITLE_BAND_H    = 30;
    public static final int TOOLBAR_BAND_H  = 32;
    public static final int FOOTER_BAND_H   = 32;

    // ── Layout: cards ───────────────────────────────────────────────────
    public static final int CARD_WIDTH  = 184;
    public static final int CARD_HEIGHT = 82;
    public static final int CARD_GAP    = 10;

    // ── Layout: tabs / search ───────────────────────────────────────────
    public static final int TAB_HEIGHT   = 20;
    public static final int TAB_GAP      = 4;
    public static final int SEARCH_WIDTH = 150;
}
