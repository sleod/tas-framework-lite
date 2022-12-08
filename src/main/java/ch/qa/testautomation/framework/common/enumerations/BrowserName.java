package ch.qa.testautomation.framework.common.enumerations;

/**
 * browser name for remote driver capability
 */
public enum BrowserName {
    CHROME("chrome"), EDGE("edge");

    private final String browserName;

    BrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getName() {
        return browserName;
    }

}
