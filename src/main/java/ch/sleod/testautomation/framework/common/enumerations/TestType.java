package ch.sleod.testautomation.framework.common.enumerations;

/**
 * enum of test types that can be used in test case content
 */
public enum TestType {
    APP("app"), WEB_APP("web_app"), MOBILE_WEB_APP("mobile_web_app"), MOBILE_IOS("mobile_ios"), MOBILE_ANDROID("mobile_android"), REST("rest");

    private final String type;

    TestType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }
}
