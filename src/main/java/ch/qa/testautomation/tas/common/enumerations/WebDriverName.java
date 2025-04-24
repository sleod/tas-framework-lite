package ch.qa.testautomation.tas.common.enumerations;

/**
 * browser name for remote driver capability
 */
public enum WebDriverName {
    CHROME("chrome"), PLAYWRIGHT("playwright");

    private final String driverName;

    WebDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getName() {
        return driverName;
    }

}
