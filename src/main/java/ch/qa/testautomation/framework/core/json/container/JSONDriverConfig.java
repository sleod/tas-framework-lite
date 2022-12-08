package ch.qa.testautomation.framework.core.json.container;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class JSONDriverConfig extends JSONContainer{
    private String platformName;
    private String platformVersion;
    private String browserVersion;
    private String deviceName;
    private String app;
    private String appName;//iOS
    private String appPackage;//android
    private String appActivity;//android
    private String grdn_uuid;//android
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
    @JsonIgnore
    private final LinkedHashMap<String, Object> capabilities = new LinkedHashMap<>();
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
            SystemLogger.trace("Set Config " + getDeviceName() + " Idle.");
        } else {
            SystemLogger.trace("Set Config " + getDeviceName() + " busy.");
        }
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
        capabilities.put("systemPort", systemPort);
    }

    public String getAutoAcceptAlerts() {
        return autoAcceptAlerts;
    }

    public void setAutoAcceptAlerts(String autoAcceptAlerts) {
        this.autoAcceptAlerts = autoAcceptAlerts;
        capabilities.put("autoAcceptAlerts", autoAcceptAlerts);
    }

    public String getNoReset() {
        return noReset;
    }

    public void setNoReset(String noReset) {
        this.noReset = noReset;
        capabilities.put("noReset", noReset);
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
        capabilities.put("bundleId", bundleId);
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
        capabilities.put("udid", udid);
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
        capabilities.put("appName", appName);
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
        capabilities.put("platformVersion", platformVersion);
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        capabilities.put("deviceName", deviceName);
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
        capabilities.put("app", app);
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
        capabilities.put("appPackage", appPackage);
    }

    public String getAppActivity() {
        return appActivity;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
        capabilities.put("appActivity", appActivity);
    }

    public String getGrdn_uuid() {
        return grdn_uuid;
    }

    public void setGrdn_uuid(String grdn_uuid) {
        this.grdn_uuid = grdn_uuid;
        capabilities.put("grdn_uuid", grdn_uuid);
    }

    public String getRealDeviceUuid() {
        return realDeviceUuid;
    }

    public void setRealDeviceUuid(String realDeviceUuid) {
        Map<String, String> map = Collections.singletonMap("realDeviceUuid", realDeviceUuid);
        this.realDeviceUuid = realDeviceUuid;
        capabilities.put("testbirds:options", map);
    }

    public String getAutomationName() {
        return automationName;
    }

    public void setAutomationName(String automationName) {
        this.automationName = automationName;
        capabilities.put("automationName", automationName);
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
