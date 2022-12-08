package ch.qa.testautomation.framework.core.json.container;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.logging.Screenshot;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.TestCaseStep;
import ch.qa.testautomation.framework.core.component.TestStepResult;
import ch.qa.testautomation.framework.core.json.customDeserializer.CustomAttachmentListDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.File;
import java.util.*;

public class JSONStepResult extends JSONContainer {
    private String name;
    private String status;
    private long start;
    private long stop;
    private List<JSONAttachment> attachments;
    private Map<String, Object> statusDetails;

    private final String logFilePath;

    public JSONStepResult(TestCaseStep testCaseStep, String logFilePath) {
        TestStepResult testStepResult = testCaseStep.getTestStepResult();
        this.name = testStepResult.getName();
        this.status = testStepResult.getStatus().text();
        this.start = testStepResult.getStart();
        this.stop = testStepResult.getStop();
        this.logFilePath = logFilePath;
        attachments = new LinkedList<>();
        addAttachments(testCaseStep);
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getStop() {
        return stop;
    }

    public void setStop(long stop) {
        this.stop = stop;
    }

    public List<JSONAttachment> getAttachments() {
        return attachments;
    }

    @JsonDeserialize(using = CustomAttachmentListDeserializer.class)
    public void setAttachments(List<JSONAttachment> attachments) {
        this.attachments = attachments;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getStatusDetails() {
        return statusDetails;
    }

    @JsonAnySetter
    public void setStatusDetails(String key, Object value) {
        if (Objects.isNull(statusDetails)) {
            statusDetails = new LinkedHashMap<>();
        }
        statusDetails.put(key, value);
    }

    @JsonIgnore
    public void setStatusDetailsMap(Map<String, Object> detailsMap) {
        statusDetails = detailsMap;
    }

    private void addAttachments(TestCaseStep testCaseStep) {
        addComment(testCaseStep);
        addLogs(testCaseStep);
        addScreenshots(testCaseStep);
    }

    private void addComment(TestCaseStep testCaseStep) {
        String comment = testCaseStep.getComment();
        if (comment != null && !comment.isEmpty()) {
            attachments.add(new JSONAttachment("Comment: " + comment, "text/json", ""));
        }
    }

    private void addLogs(TestCaseStep testCaseStep) {
        TestStepResult stepResult = testCaseStep.getTestStepResult();
        String location = new File(logFilePath).getParentFile().getAbsolutePath();
        File target = new File(location + stepResult.getName().replace("/", "-") + ".txt");
        FileOperation.writeStringToFile(stepResult.getInfo(), target);
        attachments.add(new JSONAttachment("Step Log", "text/plain", target.getAbsolutePath()));
    }

    private void addScreenshots(TestCaseStep testCaseStep) {
        TestStepResult stepResult = testCaseStep.getTestStepResult();
        String attachType = "image/" + PropertyResolver.getScreenshotFormat().toLowerCase();
        List<Screenshot> screenshots = stepResult.getScreenshots();
        for (Screenshot screenshot : screenshots) {
            attachments.add(new JSONAttachment("Screenshot", attachType, screenshot.getScreenshotFile().getAbsolutePath()));
            if (screenshot.hasPageFile()) {
                attachments.add(new JSONAttachment("Page Copy", "text/html", screenshot.getPageFile().getAbsolutePath()));
            }
        }
    }
}