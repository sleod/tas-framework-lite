package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.common.logging.Screenshot;
import ch.qa.testautomation.tas.common.utils.DateTimeUtils;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;

import java.util.LinkedList;
import java.util.List;

public class TestStepResult {

    private TestStatus testStatus = TestStatus.NO_RUN;
    private TestFailure testFailure;
    private long startTime;
    private long stopTime;
    private final List<Screenshot> screenshots = new LinkedList<>();
    private String actual = "As expected";
    private String name;
    private final int stepOrder;
    private String testMethod;
    private final StringBuilder logs = new StringBuilder();

    public TestStepResult(String name, int stepOrder) {
        this.name = name;
        this.stepOrder = stepOrder;
    }

    public StringBuilder getLogs() {
        return logs;
    }

    public String getTestMethod() {
        return testMethod;
    }

    public void setTestMethod(String testMethod) {
        this.testMethod = testMethod;
    }

    public void setStatus(TestStatus testStatus) {
        this.testStatus = testStatus;
    }

    public TestStatus getStatus() {
        return testStatus;
    }

    public List<Screenshot> getScreenshots() {
        return screenshots;
    }

    public TestFailure getTestFailure() {
        if (testFailure != null) {
            return testFailure;
        } else {
            return new TestFailure(new ApollonBaseException(ApollonErrorKeys.TEST_FAILURE_UNKNOWN));
        }
    }

    public void setTestFailure(TestFailure testFailure) {
        this.testFailure = testFailure;
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

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public void addScreenshot(Screenshot screenshot) {
        screenshots.add(screenshot);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public String getInfo() {
        return logs.toString();
    }

    public synchronized void logInfo(String line) {
        logs.append(line).append("\n");
    }

    public String getStepId() {
        return getStepOrder() + getName();
    }
}
