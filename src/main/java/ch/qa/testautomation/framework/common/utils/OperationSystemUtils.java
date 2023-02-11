package ch.qa.testautomation.framework.common.utils;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.controller.ExternAppController;

import java.io.File;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.info;

public class OperationSystemUtils {

    /**
     * clean up all remain tasks of web driver in system task list
     */
    public static void cleanUpWindowsDriverProcess() {
        String driverName = new File(PropertyResolver.getChromeDriverFileName()).getName();
        if (!driverName.endsWith(".exe")) {
            driverName += ".exe";
        }
        info("Try to clean up remain running web driver in Windows OS: " + driverName);
        ExternAppController.executeCommand("taskkill /f /fi \"pid gt 0\" /im " + driverName);
    }

    public static void cleanUpNonWindowsDriverProcess() {
        String driverName = new File(PropertyResolver.getChromeDriverFileName()).getName();
        if (driverName.endsWith(".exe")) {
            driverName = driverName.replace(".exe", "");
        }
        info("Try to clean up remain running web driver in non Windows OS: " + driverName);
        ExternAppController.executeCommand("killall '" + driverName + "'");
    }

}
