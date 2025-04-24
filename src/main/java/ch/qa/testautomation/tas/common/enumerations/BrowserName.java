package ch.qa.testautomation.tas.common.enumerations;

/**
 * browser name for remote driver capability
 */
public enum BrowserName {
    CHROME("chrome"), EDGE("msedge"), FIREFOX("firefox"), CHROMIUM("chromium");

    private final String browserName;

    BrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getName() {
        return browserName;
    }

}
