package io.github.sleod.tas.core.component;

import io.github.sleod.tas.common.enumerations.TestStatus;
import io.github.sleod.tas.common.utils.DateTimeUtils;
import io.github.sleod.tas.core.media.IgnoredScreen;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.Objects;

/**
 * TestStepResult is a class that holds the result of a test step execution.
 * It contains information about the test status, failure details, timing, screenshots, logs, and other relevant data.
 */
public class TestStepResult {
    private TestStatus testStatus;
    private TestFailure testFailure;
    @Getter
    @Setter
    private long startTime;
    @Getter
    @Setter
    private long stopTime;
    @Getter
    @Setter
    private File fullScreen;
    @Getter
    @Setter
    private File expectedScreen;
    @Getter
    @Setter
    private File actualScreen;
    @Getter
    @Setter
    private IgnoredScreen ignoredScreen;
    @Setter
    @Getter
    private String actual = "As expected";
    @Setter
    @Getter
    private String name;
    @Getter
    private final int stepOrder;
    @Setter
    @Getter
    private String testMethod;
    @Getter
    private final StringBuilder logs = new StringBuilder();

    /**
     * Constructor for TestStepResult.
     *
     * @param name      The name of the test step.
     * @param stepOrder The order of the test step in the sequence.
     */
    public TestStepResult(String name, int stepOrder) {
        this.name = name;
        this.stepOrder = stepOrder;
    }

    public void setStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    public TestStatus getStatus() {
        return Objects.requireNonNullElse(testStatus, TestStatus.NO_RUN);
    }

    public TestFailure getTestFailure() {
        if (testStatus.equals(TestStatus.NO_RUN) || testStatus.equals(TestStatus.PASS)) {
            return null;
        }else {
            return Objects.requireNonNullElseGet(testFailure, () -> new TestFailure(new ExceptionBase(ExceptionErrorKeys.TEST_FAILURE_UNKNOWN)));
        }
    }

    public void setTestFailure(TestFailure testFailure) {
        this.testFailure = testFailure;
        //in failure case , log trace to step info.
        logInfo(testFailure.getMessage());
    }

    public long getStart() {
        return startTime;
    }

    public long getStop() {
        return stopTime;
    }

    public void startNow() {
        startTime = DateTimeUtils.getNowMilli();
    }

    public void stopNow() {
        stopTime = DateTimeUtils.getNowMilli();
    }

    public String getStepLogs() {
        return logs.toString();
    }

    public synchronized void logInfo(String line) {
        logs.append(line).append(System.lineSeparator());
    }

    public String getStepId() {
        return getStepOrder() + getName();
    }
}
