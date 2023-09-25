package ch.qa.testautomation.tas.common.enumerations;

/**
 * enum of test types that can be used in test case content
 */
public enum DownloadStrategy {

    AUTO("auto"),
    AZURE_DEV_OPS("azureDevOps"),
    ONLINE("online"),
    SHARED_FILE("sharedFile");

    private final String strategy;

    DownloadStrategy(String type) {
        this.strategy = type;
    }

    public String strategy() {
        return strategy;
    }
}
