package ch.qa.testautomation.framework.common.abstraction;

import ch.qa.testautomation.framework.common.logging.SystemLogger;

import java.util.Objects;

/**
 * Parent Class for all test objects
 */
public abstract class SingleTestObject {
    public static void logStepInfo(String text) {
        SystemLogger.logStepInfo(text);
    }

    public static boolean compareAndLog(Object expected, Object actual, String message) {
        boolean isOK = String.valueOf(expected).trim().equalsIgnoreCase(String.valueOf(actual).trim());
        logStepInfo(message + " " + isSuccess(isOK) + " " + expected + " <|> " + actual);
        return isOK;
    }

    public static boolean compareAndLog(Object expected, Object actual, String message, String replaceNull) {
        boolean isOK = String.valueOf(expected).replace("null", replaceNull).trim()
                .equalsIgnoreCase(String.valueOf(actual).replace("null", replaceNull).trim());
        logStepInfo(message + " " + isSuccess(isOK) + " " + expected + " <|> " + actual);
        return isOK;
    }

    public static String isSuccess(boolean result) {
        return result ? " successful!" : " failed!";
    }

    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    public static boolean nonNull(Object obj) {
        return Objects.nonNull(obj);
    }

    public static boolean isValid(Object value) {
        return nonNull(value) && !value.toString().isEmpty();
    }
}
