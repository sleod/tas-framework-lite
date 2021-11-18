package ch.qa.testautomation.framework.core.json.container;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class JSONDriverConfig {
    @JsonProperty
    private String platformName;
    @JsonProperty
    private String platformVersion;
    @JsonProperty
    private String platform;
    @JsonProperty
    private String deviceName;
    @JsonProperty
    private String app;
    @JsonProperty
    private String appName;
    @JsonProperty
    private String appPackage;
    @JsonProperty
    private String appActivity;
    @JsonProperty
    private String grdn_uuid;
    @JsonProperty
    private String realDeviceUuid;
    @JsonProperty
    private String udid;
    @JsonProperty
    private String automationName;
    @JsonProperty
    private String browser;
    @JsonProperty
    private String browserName;
    @JsonProperty
    private String hubURL;
    @JsonProperty
    private String chromeDriverExecutable;
    @JsonProperty
    private String ieDriverBinFile;
    @JsonProperty
    private String bundleId;
    @JsonProperty
    private String webDriverVersion;
    @JsonProperty
    private String noReset;

    @JsonIgnore
    private LinkedHashMap<String, Object> capabilities = new LinkedHashMap<>();

    public String getIsNoReset() {
        if (noReset == null) {
            noReset = "";
        }
        return noReset;
    }

    public void setNoReset(String noReset) {
        this.noReset = noReset;
        capabilities.put("noReset", noReset);

    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
        capabilities.put("platform", platform);
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

    public Map<String, Object> getCapabilities() {
        return capabilities;
    }
}
