package ch.sleod.testautomation.framework.core.component;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class TestFailure {

    private String message;
    private Throwable exception;

    public TestFailure(String message, Throwable exception) {
        this.message = message;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }

    public String getTrace() {
        return ExceptionUtils.getStackTrace(exception);
    }
}
