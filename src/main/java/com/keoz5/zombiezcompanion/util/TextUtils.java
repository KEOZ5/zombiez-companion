package com.keoz5.zombiezcompanion.util;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class TextUtils {

    private TextUtils() {}

    /** Strips Minecraft § color codes from a raw string. */
    public static String strip(String text) {
        if (text == null) return "";
        return text.replaceAll("§[0-9a-fk-orA-FK-OR]", "").trim();
    }

    /** Returns the plain string of a Text component, stripped of formatting. */
    public static String plain(Text text) {
        return strip(text.getString());
    }

    public static Text colored(String msg, Formatting formatting) {
        return Text.literal(msg).formatted(formatting);
    }
}
