package io.github.sleod.tas.web;

import io.github.sleod.tas.core.component.DriverManager;
import io.github.sleod.tas.core.json.container.JSONDriverConfig;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import io.github.sleod.tas.intefaces.DriverProvider;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.sleod.tas.common.logging.SystemLogger.info;

public class RemoteWebDriverProvider implements DriverProvider {

    //    private RemoteWebDriver driver = null;

    private static final Map<String, RemoteWebDriver> drivers = new ConcurrentHashMap<>();
    private static final Map<String, JSONDriverConfig> configMap = new ConcurrentHashMap<>();

    public RemoteWebDriverProvider(JSONDriverConfig jsonDriverConfig) {
        String tid = Thread.currentThread().getName();
        info("Set Driver Config for " + tid + " : " + jsonDriverConfig.getDeviceName() + " " + jsonDriverConfig.getPlatformVersion());
        configMap.put(tid, jsonDriverConfig);
    }

    public void setDriver(RemoteWebDriver driver) {
        drivers.put(Thread.currentThread().getName(), driver);
        info("Save remote driver for: " + Thread.currentThread().getName());
    }

    @Override
    @SuppressWarnings("unchecked")
    public RemoteWebDriver getDriver() {
        String tid = Thread.currentThread().getName();
        if (drivers.get(tid) == null) {
            info("Init remote driver for: " + tid);
            initialize();
        } else {
            info("Get remote driver for: " + tid);
        }
        return drivers.get(tid);
    }

    public JSONDriverConfig getConfig() {
        return configMap.get(Thread.currentThread().getName());
    }

    @Override
    public void close() {
        String tid = Thread.currentThread().getName();
        if (Objects.nonNull(drivers.get(tid))) {
            info("Close remote driver for: " + tid);
            drivers.get(tid).quit();
            drivers.remove(tid);
        }
    }

    @Override
    public void initialize() {
        JSONDriverConfig config = getConfig();
        if (Objects.nonNull(config)) {
            DriverManager.setCurrentPlatform(config.getPlatformName());
            info("Initialize Remote Web Driver with Config: " + config.getDeviceName());
            if (config.getHubURL() != null && !config.getHubURL().isEmpty()) {
                DesiredCapabilities capabilities = new DesiredCapabilities(config.getCapabilities());
                capabilities.asMap().forEach((key, value) -> info("Capability: " + key + " -> " + value));
                RemoteWebDriver driver = null;
                try {
                    driver = new RemoteWebDriver(new URL(capabilities.getCapability("hubURL").toString()), capabilities);
                    setDriver(driver);
                } catch (MalformedURLException | WebDriverException ex) {
                    if (driver != null) {
                        driver.close();
                    }
                    throw new ExceptionBase(ExceptionErrorKeys.INITIALIZATION_FAILED, ex, "Remote Web Driver");
                }
            }
        }
    }
}
