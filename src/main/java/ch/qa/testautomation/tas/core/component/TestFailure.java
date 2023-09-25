package ch.qa.testautomation.tas.core.component;

import org.apache.commons.lang3.exception.ExceptionUtils;

public class TestFailure {

    private final Throwable exception;

    public TestFailure(Throwable exception) {
        this.exception = exception;
    }

    public String getMessage() {
        if (exception != null && exception.getMessage() != null)
            return exception.getMessage();
        else return "Test Failure with unknown exception!!";
    }

    public Throwable getException() {
        return exception;
    }

    public String getTrace() {
        return ExceptionUtils.getStackTrace(exception);
    }
}
