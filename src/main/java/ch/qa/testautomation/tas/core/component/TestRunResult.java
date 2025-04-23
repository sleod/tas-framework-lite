package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.enumerations.TestStatus;

import java.io.File;
import java.time.Instant;
import java.util.*;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.getSimpleCustomInfo;

public class TestRunResult {
    private TestStatus status = TestStatus.NO_RUN;
    private final List<TestStepResult> stepResults = new LinkedList<>();
    private String name;
    private List<File> attachments = new LinkedList<>();
    private long startTime;
    private long stopTime;
    private TestFailure testFailure;
    private String videoFilePath;
    private String logFilePath;
    private String begin;
    private String end;
    private String description;
    private String threadName;
    private Map<String, Object> parameters = Collections.emptyMap();

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public List<TestStepResult> getStepResults() {
        return stepResults;
    }

    public TestFailure getTestFailure() {
        return testFailure;
    }

    public void setTestFailure(TestFailure testFailure) {
        this.testFailure = testFailure;
    }

    public void addStepResults(TestStepResult stepResult) {
        this.stepResults.add(stepResult);
        if (status.equals(TestStatus.NO_RUN) || !status.equals(TestStatus.FAIL)
                && !status.equals(TestStatus.SKIPPED) && !status.equals(TestStatus.BROKEN)) {
            setStatus(stepResult.getStatus());
        }
        File screenshot = stepResult.getFullScreen();
        if (screenshot != null) {
            attachments.add(screenshot);
        }
        if (stepResult.getStatus().equals(TestStatus.FAIL)) {
            setTestFailure(stepResult.getTestFailure());
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<File> attachments) {
        this.attachments = attachments;
    }

    public void startNow(String logText) {
        startTime = Instant.now().toEpochMilli();
        begin = getSimpleCustomInfo("TRACE", logText);
    }

    public void stopNow(String logText) {
        stopTime = Instant.now().toEpochMilli();
        end = getSimpleCustomInfo("TRACE", logText);
    }

    public long getStart() {
        return startTime;
    }

    public long getStop() {
        return stopTime;
    }

    public float getRunDuration() {
        return Instant.ofEpochMilli(stopTime - startTime).getEpochSecond();
    }

    public String getVideoFilePath() {
        return videoFilePath;
    }

    public void setVideoFilePath(String videoFilePath) {
        this.videoFilePath = videoFilePath;
    }

    public String getLogFilePath() {
        return logFilePath;
    }

    public void setLogFilePath(String logFilePath) {
        this.logFilePath = logFilePath;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public String getThreadName() {
        return threadName;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setParameters(Map<String, Object> first) {
        this.parameters = first;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }
}
