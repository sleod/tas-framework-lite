package ch.qa.testautomation.tas.common.utils;

import ch.qa.testautomation.tas.configuration.PropertyResolver;

import java.io.File;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.core.controller.ExternAppController.executeCommand;

public class OperationSystemUtils {

    /**
     * clean up all remain tasks of web driver in system task list
     */
    public static void cleanUpDriverProcess() {
        String driverName = new File(PropertyResolver.getChromeDriverFileName()).getName();
        info("Try to clean up remain running web driver in non Windows OS: " + driverName);
        if (PropertyResolver.isWindows()) {
            executeCommand("taskkill /f /fi \"pid gt 0\" /im " + driverName);
        } else {
            executeCommand("pkill '" + driverName + "*'");
        }
    }

    public static void cleanBrowserProcess() {
        if (PropertyResolver.isWindows()) {
            executeCommand("taskkill /f /fi \"pid gt 0\" /im " + PropertyResolver.getWebDriverName() + "*.exe");
        } else {
            executeCommand("pkill '" + PropertyResolver.getWebDriverName() + "*'");
            executeCommand("pkill 'Google Chrome*'");
        }
    }
}
