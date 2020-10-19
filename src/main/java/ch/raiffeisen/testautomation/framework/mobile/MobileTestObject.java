package ch.raiffeisen.testautomation.framework.mobile;

import ch.raiffeisen.testautomation.framework.common.abstraction.SingleTestObject;

public abstract class MobileTestObject extends SingleTestObject {
    private String appName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
