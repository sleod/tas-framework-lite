package ch.qa.testautomation.tas.core.assertion;


import org.junit.jupiter.api.Assertions;

public class Assertion extends Assertions {
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
