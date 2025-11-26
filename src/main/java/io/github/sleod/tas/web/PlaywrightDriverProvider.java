package io.github.sleod.tas.web;

import com.microsoft.playwright.Playwright;
import io.github.sleod.tas.common.utils.NodeLocator;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import io.github.sleod.tas.intefaces.DriverProvider;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static io.github.sleod.tas.common.logging.SystemLogger.info;

public class PlaywrightDriverProvider implements DriverProvider {

    private static final ThreadLocal<PlaywrightDriver> drivers = new ThreadLocal<>();
    private Playwright playwright;
    private final Set<String> driverOptions = new HashSet<>();

    @Override
    @SuppressWarnings("unchecked")
    public PlaywrightDriver getDriver() {
        String tid = Thread.currentThread().getName();
        if (drivers.get() == null) {
            info("Init Playwright driver for: " + tid);
            initialize();
        } else {
            info("Get Playwright driver for: " + tid);
        }
        return drivers.get();
    }

    @Override
    public void close() {
        info("Close Playwright driver.");
        getDriver().quit();
        drivers.remove();
        playwright.close();
    }

    @Override
    public void initialize() {
        info("Initialize Playwright driver.");
        if (PropertyResolver.isSkipPlaywrightBrowserDownload()) {
            playwright = Playwright.create(new Playwright.CreateOptions().setEnv(getPlaywrightEnv()));
        } else {
            playwright = Playwright.create();
        }
        drivers.set(new PlaywrightDriver(playwright));
    }

    private Map<String, String> getPlaywrightEnv() {
        String nodePath = PropertyResolver.getNodeJSBinFilePath();
        if (nodePath == null || nodePath.isBlank()) {
            NodeLocator.NodeInfo info = NodeLocator.findNode();
            if (info == null) {
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE,
                        "Node bin file from node.js was not found.\nPlease use '.setNodeJSBinFilePath()' within setUpFramework in Runner");
            } else {
                nodePath = info.getExecutable().toString();
            }
        }
        return Map.of(
                "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1",
                "PLAYWRIGHT_NODEJS_PATH", nodePath);
    }

}
