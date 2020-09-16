package ch.sleod.testautomation.framework.common.utils;

import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.common.logging.SystemLogger;

import java.io.File;
import java.io.IOException;

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
            SystemLogger.trace("Try to clean up remain running driver services: " + driverName);
            Runtime.getRuntime().exec("taskkill /f /fi \"pid gt 0\" /im " + driverName);
        } catch (IOException e) {
            SystemLogger.error(e);
        }

    }
}
