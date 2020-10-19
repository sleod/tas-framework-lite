package ch.raiffeisen.testautomation.framework.common.enumerations;

/**
 * browser name for remote driver capability
 */
public enum BrowserName {
    FIREFOX("firefox"), CHROME("chrome"), IE("internetExplorer"), ANDROID("android"), EDGE("edge"), IPAD("ipad"), IPHONE("iphone"), SAFARI("safari");

    private final String browserName;

    BrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String value() {
        return browserName;
    }
}
