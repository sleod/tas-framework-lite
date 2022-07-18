package ch.qa.testautomation.framework.common.abstraction;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

import java.util.Objects;

public abstract class SingleTestObject {
    public static void logStepInfo(String text) {
        SystemLogger.logStepInfo(Thread.currentThread().getName(), text);
    }

    public static boolean compareAndLog(Object expected, Object actual, String message) {
        if (Objects.isNull(expected) || expected.equals("null")) {
            expected = "";
        }
        boolean isOK = Objects.equals(expected, actual);
        logStepInfo(message + " " + isSuccess(isOK) + " " + expected + " <|> " + actual);
        return isOK;
    }

    public static String isSuccess(boolean result) {
        if (!result) {
            return "Failed!";
        } else {
            return "Successfully!";
        }
    }
}
