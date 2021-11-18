package ch.qa.testautomation.framework.core.assertion;

import org.junit.Assert;

public class Assertion extends Assert {
    public static void assessTrue(String message, boolean condition) {
        if (!condition) {
            broken(message);
        }
    }

    private static void broken(String message) {
        if (message == null) {
            throw new KnownIssueException();
        }
        throw new KnownIssueException(message);
    }

}
