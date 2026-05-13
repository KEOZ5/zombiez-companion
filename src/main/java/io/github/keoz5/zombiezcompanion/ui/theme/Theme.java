package io.github.keoz5.zombiezcompanion.ui.theme;

/**
 * Single source of truth for the mod's UI palette and sizing.
 *
 * <p>Colors are ARGB ints ({@code 0xAARRGGBB}) as expected by Minecraft's
 * {@code DrawContext}. Sizes are in screen pixels (pre-GUI-scale).
 *
 * <p>Palette: muted blue-grey ("dashboard sober"). The accent is a desaturated
 * blue used for selected tabs, focus borders, slider progress. Green is kept
 * intentionally — but only for positive enable states on module cards, so it
 * reads as a clear "this module is on" without flooding the rest of the UI.
 */
public final class Theme {

    private Theme() {}

    // ── Backgrounds ──────────────────────────────────────────────────────
    public static final int BG_DIM          = 0xC0000000;
    public static final int BG_PANEL        = 0xFF1A1F26;
    public static final int BG_BAND         = 0xFF13171D;
    public static final int BG_CONTENT      = 0xFF1D232B;
    public static final int BG_CARD         = 0xFF222932;
    public static final int BG_CARD_HOVER   = 0xFF2A323D;
    public static final int BG_BTN          = 0xFF252C36;
    public static final int BG_BTN_HOVER    = 0xFF2F3845;
    public static final int BG_INPUT        = 0xFF111418;
    public static final int BG_TAB_OFF      = 0xFF222932;
    public static final int BG_TAB_OFF_HOV  = 0xFF2A323D;
    public static final int BG_TAB_ON       = 0xFF2D5288;
    public static final int BG_TAB_ON_HOV   = 0xFF36629E;

    // ── Borders / dividers ──────────────────────────────────────────────
    public static final int BORDER          = 0xFF2A323D;
    public static final int BORDER_STRONG   = 0xFF3A4250;
    public static final int BORDER_FOCUS    = 0xFF5A8FCC;

    // ── Text ─────────────────────────────────────────────────────────────
    public static final int TEXT_PRIMARY    = 0xFFE3E8EF;
    public static final int TEXT_MUTED      = 0xFF7B8696;
    public static final int TEXT_DISABLED   = 0xFF4A5160;
    public static final int TEXT_ACCENT     = 0xFF89B0E0;

    // ── Module enable state — discreet green for ON, muted red for OFF ──
    public static final int STATE_ON_BG     = 0xFF3E7A4D;
    public static final int STATE_ON_BG_H   = 0xFF4A8F5A;
    public static final int STATE_OFF_BG    = 0xFF7A3E3E;
    public static final int STATE_OFF_BG_H  = 0xFF8F4A4A;

    // ── Accent fill — used for slider progress, generic positive accents ──
    public static final int ACCENT_FILL     = 0xFF2D5288;
    public static final int ACCENT_FILL_H   = 0xFF36629E;

    // ── Layout: paddings ────────────────────────────────────────────────
    public static final int PADDING_LG = 14;
    public static final int PADDING_MD = 10;
    public static final int PADDING_SM = 6;

    // ── Layout: panel chrome ────────────────────────────────────────────
    public static final int PANEL_MARGIN_MIN = 24;
    public static final int PANEL_MAX_WIDTH  = 1000;
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
