package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.common.logging.Screenshot;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.getSimpleCustomInfo;

public class TestRunResult {
    @Setter
    @Getter
    private TestStatus status = TestStatus.NO_RUN;
    @Getter
    private final List<TestStepResult> stepResults = new LinkedList<>();
    @Setter
    @Getter
    private String name;
    @Setter
    @Getter
    private List<File> attachments = new LinkedList<>();
    private long startTime;
    private long stopTime;
    @Setter
    @Getter
    private TestFailure testFailure;
    @Setter
    @Getter
    private String videoFilePath;
    @Setter
    @Getter
    private String logFilePath;
    @Getter
    private String begin;
    @Getter
    private String end;
    @Getter
    @Setter
    private String description;
    @Setter
    @Getter
    private String threadName;
    @Setter
    @Getter
    private Map<String, Object> parameters = Collections.emptyMap();


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

}
