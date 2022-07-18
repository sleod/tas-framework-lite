package ch.qa.testautomation.framework.core.json.container;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.logging.Screenshot;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.component.TestCaseStep;
import ch.qa.testautomation.framework.core.component.TestStepResult;
import ch.qa.testautomation.framework.core.json.customDeserializer.CustomAttachmentListDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class JSONStepResult {
    @JsonProperty
    private String name;
    @JsonProperty
    private String status;
    @JsonProperty
    private long start;
    @JsonProperty
    private long stop;
    @JsonProperty
    private List<JSONAttachment> attachments = new LinkedList<>();
    @JsonProperty
    private Map<String, Object> statusDetails = new LinkedHashMap<>();

    private final String logFilePath;

    public JSONStepResult(TestCaseStep testCaseStep, String logFilePath) throws IOException {

        TestStepResult testStepResult = testCaseStep.getTestStepResult();
        this.name = testStepResult.getName();
        this.status = testStepResult.getStatus().text();
        this.start = testStepResult.getStart();
        this.stop = testStepResult.getStop();
        this.logFilePath = logFilePath;

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
        this.statusDetails.put(key, value);
    }

    @JsonIgnore
    public void setStatusDetailsMap(Map<String, Object> detailsMap) {
        this.statusDetails = detailsMap;
    }

    private void addAttachments(TestCaseStep testCaseStep) throws IOException {

        attachments = new LinkedList<>();
        addComment(testCaseStep);
        addLogs(testCaseStep);
        addScreenshots(testCaseStep);
    }

    private void addComment(TestCaseStep testCaseStep) {
        String comment = testCaseStep.getComment();
        if(comment != null && !comment.isEmpty()){
            attachments.add(new JSONAttachment("Comment: " + comment, "text/json", ""));
        }
    }

    private void addLogs(TestCaseStep testCaseStep) throws IOException {

        TestStepResult stepResult = testCaseStep.getTestStepResult();
        String location = new File(logFilePath).getParentFile().getAbsolutePath();
        File target = new File(location + stepResult.getName() + ".txt");
        FileOperation.writeBytesToFile(stepResult.getInfo().getBytes(), target);
        attachments.add(new JSONAttachment("Step Log", "text/plain", target.getAbsolutePath()));
    }

    private void addScreenshots(TestCaseStep testCaseStep) {
        TestStepResult stepResult = testCaseStep.getTestStepResult();
        String attachType = "image/" + PropertyResolver.getDefaultScreenshotFormat().toLowerCase();
        List<Screenshot> screenshots = stepResult.getScreenshots();
        for (Screenshot screenshot : screenshots) {
            attachments.add(new JSONAttachment("Screenshot", attachType, screenshot.getScreenshotFile().getAbsolutePath()));
            if (screenshot.hasPageFile()) {
                attachments.add(new JSONAttachment("Page Copy", "text/html", screenshot.getPageFile().getAbsolutePath()));
            }
        }
    }
}