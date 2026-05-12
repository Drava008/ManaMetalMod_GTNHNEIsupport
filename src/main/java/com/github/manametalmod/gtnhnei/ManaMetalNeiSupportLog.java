package com.github.manametalmod.gtnhnei;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ManaMetalNeiSupportLog {

    private static final Logger LOGGER = LogManager.getLogger(ManaMetalNeiSupportMod.MODID);

    private ManaMetalNeiSupportLog() {}

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }
}
