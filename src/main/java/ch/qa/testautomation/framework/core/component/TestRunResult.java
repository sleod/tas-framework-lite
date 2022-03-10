package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.enumerations.TestStatus;
import ch.qa.testautomation.framework.common.logging.Screenshot;
import junit.framework.TestResult;

import java.io.File;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.getSimpleCustomInfo;

public class TestRunResult extends TestResult {
    private TestStatus status = TestStatus.NO_RUN;
    private List<TestStepResult> stepResults = new LinkedList<>();
    private String name;
    private List<File> attachments = new LinkedList<>();
    private long startTime;
    private long stopTime;
    private TestFailure testFailure = null;
    private String videoFilePath = "";
    private String logFilePath = "";
    private String begin = "";
    private String end = "";
    private String description = "";

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
        if (status.equals(TestStatus.NO_RUN) || !status.equals(TestStatus.FAIL)) {
            setStatus(stepResult.getStatus());
        }
        if (!stepResult.getScreenshots().isEmpty()) {
            for (Screenshot screenshot : stepResult.getScreenshots()) {
                attachments.add(screenshot.getScreenshotFile());
                if (screenshot.hasPageFile())
                    attachments.add(screenshot.getPageFile());
            }
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
        begin = getSimpleCustomInfo("INFO", logText);
    }

    public void stopNow(String logText) {
        stopTime = Instant.now().toEpochMilli();
        end = getSimpleCustomInfo("INFO", logText);
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
}
