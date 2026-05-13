package com.keoz5.zombiezcompanion.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("ZombieZCompanion");

    private ModLogger() {}

    public static void info(String msg)  { LOGGER.info(msg); }
    public static void warn(String msg)  { LOGGER.warn(msg); }
    public static void error(String msg) { LOGGER.error(msg); }
    public static void debug(String msg) { LOGGER.debug(msg); }
}
