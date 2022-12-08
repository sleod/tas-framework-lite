package ch.qa.testautomation.framework.common.enumerations;

import java.util.HashSet;
import java.util.Set;

/**
 * enum of test types that can be used in test case content
 */
public enum TestType {
    APP("app"), WEB_APP("web_app"), MOBILE_IOS("mobile_ios"), MOBILE_ANDROID("mobile_android"), MOBILE_APP("mobile_app"), REST("rest");

    private final String type;

    TestType(String type) {
        this.type = type;
    }

    public String type() {
        return type;
    }

    public static Set<String> types() {
        HashSet<String> set = new HashSet<>(TestType.values().length);
        for (TestType type : TestType.values()) {
            set.add(type.type());
        }
        return set;
    }

    public static boolean isContainType(String typeName) {
        for (TestType type : TestType.values()) {
            if (type.type().equals(typeName)) {
                return true;
            }
        }
        return false;
    }
}
