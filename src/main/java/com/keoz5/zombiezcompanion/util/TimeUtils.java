package com.keoz5.zombiezcompanion.util;

public final class TimeUtils {

    private TimeUtils() {}

    /** Formats a duration in milliseconds to "HH:mm:ss" or "mm:ss". */
    public static String formatDuration(long durationMs) {
        long totalSeconds = durationMs / 1000;
        long hours   = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%d:%02d", minutes, seconds);
    }

    /** Formats a countdown in seconds to "Xm Ys" or "Xs". */
    public static String formatCountdown(int totalSeconds) {
        if (totalSeconds <= 0) return "0s";
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes > 0) return minutes + "m " + seconds + "s";
        return seconds + "s";
    }
}
