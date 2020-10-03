package ch.sleod.testautomation.framework.common.utils;

import ch.sleod.testautomation.framework.configuration.PropertyResolver;

import java.io.File;
import java.io.IOException;

import static ch.sleod.testautomation.framework.common.logging.SystemLogger.error;
import static ch.sleod.testautomation.framework.common.logging.SystemLogger.trace;

public class WindowsUtils {

    /**
     * clean up all remain tasks of web driver in system task list
     */
    public static void cleanUpWindowsDriverProcess() {
        String driverName = new File(PropertyResolver.getChromeDriverFileName()).getName();
        if (PropertyResolver.isWindows()) {
            driverName += ".exe";
        }
        try {
            trace("Try to clean up remain running driver services: " + driverName);
            Runtime.getRuntime().exec("taskkill /f /fi \"pid gt 0\" /im " + driverName);
        } catch (IOException e) {
            error(e);
        }

    }
}
