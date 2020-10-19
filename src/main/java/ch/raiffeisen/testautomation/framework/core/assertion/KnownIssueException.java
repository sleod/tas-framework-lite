package ch.raiffeisen.testautomation.framework.core.assertion;

public class KnownIssueException extends AssertionError {
    public KnownIssueException() {

    }

    public KnownIssueException(String message) {
        super(message);
    }
}
