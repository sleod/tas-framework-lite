package io.github.sleod.tas.core.component;

import io.github.sleod.tas.common.enumerations.TestStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static io.github.sleod.tas.common.logging.SystemLogger.getSimpleCustomInfo;

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
    @Getter
    @Setter
    private Map<String, Object> parameters = Collections.emptyMap();

    /**
     * add test step result to the test run result
     * @param stepResult the test step result to add
     */
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

    /**
     * start the test run now
     * @param logText the log text to add to the start log entry
     */
    public void startNow(String logText) {
        startTime = Instant.now().toEpochMilli();
        begin = getSimpleCustomInfo("TRACE", logText);
    }

    /**
     * stop the test run now
     * @param logText the log text to add to the stop log entry
     */
    public void stopNow(String logText) {
        stopTime = Instant.now().toEpochMilli();
        end = getSimpleCustomInfo("TRACE", logText);
    }

    /**
     * get the test step start time
     * @return start time in milliseconds
     */
    public long getStart() {
        return startTime;
    }

    /**
     * get the test step stop time
     * @return stop time in milliseconds
     */
    public long getStop() {
        return stopTime;
    }

    /**
     * get the test run duration in seconds
     * @return duration in seconds
     */
    public float getRunDuration() {
        return Instant.ofEpochMilli(stopTime - startTime).getEpochSecond();
    }

}
