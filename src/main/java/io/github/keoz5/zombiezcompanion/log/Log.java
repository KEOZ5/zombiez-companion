package io.github.keoz5.zombiezcompanion.log;

import io.github.keoz5.zombiezcompanion.ModInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BooleanSupplier;

/**
 * Single logging facade for the mod.
 *
 * <p>Info / warn / error always go through. Debug lines are gated by the
 * {@code debugMode} config flag and tagged with a {@link LogCategory} so users
 * can filter the log file when collecting data.
 *
 * <p>Format of debug lines: {@code [ZombieZ][DEBUG][<Category>] <message>}.
 */
public final class Log {

    private static final Logger SLF4J = LoggerFactory.getLogger(ModInfo.MOD_NAME);
    private static BooleanSupplier debugFlag = () -> false;

    private Log() {}

    public static void bindDebugFlag(BooleanSupplier supplier) {
        debugFlag = supplier;
    }

    public static void info(String msg)                  { SLF4J.info(msg); }
    public static void warn(String msg)                  { SLF4J.warn(msg); }
    public static void warn(String msg, Throwable t)     { SLF4J.warn(msg, t); }
    public static void error(String msg)                 { SLF4J.error(msg); }
    public static void error(String msg, Throwable t)    { SLF4J.error(msg, t); }

    public static boolean isDebugEnabled() {
        return debugFlag.getAsBoolean();
    }

    public static void debug(LogCategory category, String msg) {
        if (!isDebugEnabled()) return;
        SLF4J.info("[ZombieZ][DEBUG][{}] {}", category.tag(), msg);
    }
}
