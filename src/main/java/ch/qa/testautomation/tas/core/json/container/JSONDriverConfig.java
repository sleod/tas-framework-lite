package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.configuration.PropertyResolver;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;

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
    private String hubURL;
    private String chromeDriverExecutable;
    private String ieDriverBinFile;
    private String bundleId;//iOS
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

    public String getConfigType() {
        return configType;
    }

    public void setConfigType(String configType) {
        this.configType = configType;
        capabilities.put("configType", configType);
    }

    public boolean isIdle() {
        return isIdle;
    }

    public void setIdle(boolean idle) {
        isIdle = idle;
        if (idle) {
            info("Set Config " + getDeviceName() + " Idle.");
        } else {
            info("Set Config " + getDeviceName() + " busy.");
        }
    }

    public String getPlatformArchitecture() {
        return platformArchitecture;
    }

    public void setPlatformArchitecture(String platformArchitecture) {
        this.platformArchitecture = platformArchitecture;
        setTestBirdsOptions("platformArchitecture", platformArchitecture);
    }

    public String getPlatformFamily() {
        return platformFamily;
    }

    public void setPlatformFamily(String platformFamily) {
        this.platformFamily = platformFamily;
        setTestBirdsOptions("platformFamily", platformFamily);
    }

    public String getWdaLocalPort() {
        return wdaLocalPort;
    }

    public void setWdaLocalPort(String wdaLocalPort) {
        this.wdaLocalPort = wdaLocalPort;
        capabilities.put("wdaLocalPort", wdaLocalPort);
    }

    public String getSystemPort() {
        return systemPort;
    }

    public void setSystemPort(String systemPort) {
        this.systemPort = systemPort;
        capabilities.put("appium:systemPort", systemPort);
    }

    public String getAutoAcceptAlerts() {
        return autoAcceptAlerts;
    }

    public void setAutoAcceptAlerts(String autoAcceptAlerts) {
        this.autoAcceptAlerts = autoAcceptAlerts;
        capabilities.put("appium:autoAcceptAlerts", autoAcceptAlerts);
    }

    public String getNoReset() {
        return noReset;
    }

    public void setNoReset(String noReset) {
        this.noReset = noReset;
        capabilities.put("appium:noReset", noReset);
    }

    public String getWebDriverVersion() {
        return webDriverVersion;
    }

    public void setWebDriverVersion(String webDriverVersion) {
        this.webDriverVersion = webDriverVersion;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
        capabilities.put("appium:bundleId", bundleId);
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
        capabilities.put("appium:udid", udid);
    }

    public String getChromeDriverExecutable() {
        return chromeDriverExecutable;
    }

    public void setChromeDriverExecutable(String chromeDriverBinFile) {
        this.chromeDriverExecutable = chromeDriverBinFile;
        capabilities.put("chromeDriverExecutable", chromeDriverBinFile);
    }

    public String getIeDriverBinFile() {
        return ieDriverBinFile;
    }

    public void setIeDriverBinFile(String ieDriverBinFile) {
        this.ieDriverBinFile = ieDriverBinFile;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
        capabilities.put("appium:appName", appName);
    }

    public String getHubURL() {
        return hubURL;
    }

    public void setHubURL(String hubURL) {
        this.hubURL = hubURL;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
        capabilities.put("platformName", platformName);
    }

    public String getPlatformVersion() {
        return platformVersion;
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

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        capabilities.put("appium:deviceName", deviceName);
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
        capabilities.put("appium:app", app);
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
        capabilities.put("appium:appPackage", appPackage);
    }

    public String getAppActivity() {
        return appActivity;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
        capabilities.put("appium:appActivity", appActivity);
    }

    public String getRealDeviceUuid() {
        return realDeviceUuid;
    }

    public void setRealDeviceUuid(String realDeviceUuid) {
        this.realDeviceUuid = realDeviceUuid;
        setTestBirdsOptions("realDeviceUuid", realDeviceUuid);
    }

    public String getAutomationName() {
        return automationName;
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
        capabilities.put("appium:automationName", automationName);
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
        capabilities.put("browser", browser);
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
        capabilities.put("browserName", browserName);
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
        capabilities.put("browserVersion", browserVersion);
    }

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }

}
