package io.github.sleod.tas.core.json.container;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.sleod.tas.configuration.PropertyResolver;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static io.github.sleod.tas.common.logging.SystemLogger.info;

@Getter
public class JSONDriverConfig extends JSONContainer {
    private String platformName;
    private String platformVersion;
    private String browserVersion;
    private String deviceName;
    private String app;
    private String appName;//iOS
    private String appPackage;//android
    private String appActivity;//android
    private String realDeviceUuid;
    private String udid;//iOS
    private String automationName;
    private String browser;
    private String browserName;
    @Setter
    private String hubURL;
    private String chromeDriverExecutable;
    @Setter
    private String ieDriverBinFile;
    private String bundleId;//iOS
    @Setter
    private String webDriverVersion;
    private String noReset;
    private String autoAcceptAlerts;
    private String wdaLocalPort; // iOS
    private String systemPort; //android
    private String configType;
    private String platformArchitecture;
    private String platformFamily;
    @JsonIgnore
    private final Map<String, Object> capabilities = new LinkedHashMap<>();
    @JsonIgnore
    private boolean isIdle = true;

    public void setConfigType(String configType) {
        this.configType = configType;
        capabilities.put("configType", configType);
    }

    public void setIdle(boolean idle) {
        isIdle = idle;
        if (idle) {
            info("Set Config " + getDeviceName() + " Idle.");
        } else {
            info("Set Config " + getDeviceName() + " busy.");
        }
    }

    public void setPlatformArchitecture(String platformArchitecture) {
        this.platformArchitecture = platformArchitecture;
        setTestBirdsOptions("platformArchitecture", platformArchitecture);
    }

    public void setPlatformFamily(String platformFamily) {
        this.platformFamily = platformFamily;
        setTestBirdsOptions("platformFamily", platformFamily);
    }

    public void setWdaLocalPort(String wdaLocalPort) {
        this.wdaLocalPort = wdaLocalPort;
        capabilities.put("wdaLocalPort", wdaLocalPort);
    }

    public void setSystemPort(String systemPort) {
        this.systemPort = systemPort;
        capabilities.put("appium:systemPort", systemPort);
    }

    public void setAutoAcceptAlerts(String autoAcceptAlerts) {
        this.autoAcceptAlerts = autoAcceptAlerts;
        capabilities.put("appium:autoAcceptAlerts", autoAcceptAlerts);
    }

    public void setNoReset(String noReset) {
        this.noReset = noReset;
        capabilities.put("appium:noReset", noReset);
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
        capabilities.put("appium:bundleId", bundleId);
    }

    public void setUdid(String udid) {
        this.udid = udid;
        capabilities.put("appium:udid", udid);
    }

    public void setChromeDriverExecutable(String chromeDriverBinFile) {
        this.chromeDriverExecutable = chromeDriverBinFile;
        capabilities.put("chromeDriverExecutable", chromeDriverBinFile);
    }

    public void setAppName(String appName) {
        this.appName = appName;
        capabilities.put("appium:appName", appName);
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
        capabilities.put("platformName", platformName);
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
        capabilities.put("appium:platformVersion", platformVersion);
        setTestBirdsOptions("platformVersion", platformVersion);
    }

    private void setTestBirdsOptions(String key, String value) {
        if (PropertyResolver.isRemoteDeviceEnabled()) {//avoid local run with remote device setting
            capabilities.putIfAbsent("testbirds:options", new HashMap<String, String>(4));
            ((Map<String, String>) capabilities.get("testbirds:options")).put(key, value);
        }
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        capabilities.put("appium:deviceName", deviceName);
    }

    public void setApp(String app) {
        this.app = app;
        capabilities.put("appium:app", app);
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
        capabilities.put("appium:appPackage", appPackage);
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
        capabilities.put("appium:appActivity", appActivity);
    }

    public void setRealDeviceUuid(String realDeviceUuid) {
        this.realDeviceUuid = realDeviceUuid;
        setTestBirdsOptions("realDeviceUuid", realDeviceUuid);
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
        capabilities.put("appium:automationName", automationName);
    }

    public void setBrowser(String browser) {
        this.browser = browser;
        capabilities.put("browser", browser);
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
        capabilities.put("browserName", browserName);
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
        capabilities.put("browserVersion", browserVersion);
    }

}
