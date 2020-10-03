package ch.sleod.testautomation.framework.core.json.container;

import ch.sleod.testautomation.framework.common.logging.Screenshot;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.component.TestStepResult;
import ch.sleod.testautomation.framework.core.json.customDeserializer.CustomAttachmentListDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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

    public JSONStepResult(TestStepResult testStepResult) {
        this.name = testStepResult.getName();
        this.status = testStepResult.getStatus().text();
        this.start = testStepResult.getStart();
        this.stop = testStepResult.getStop();
        addAttachments(testStepResult);
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

    private void addAttachments(TestStepResult stepResult) {
        List<Screenshot> screenshots = stepResult.getScreenshots();
        attachments = new LinkedList<>();
        String[] lines = stepResult.getInfo().split("\\n");
        for (String line : lines) {
            attachments.add(new JSONAttachment(line, "plain/text", ""));
        }
        if (status.equalsIgnoreCase("failed") || status.equalsIgnoreCase("broken")) {
            attachments.add(new JSONAttachment("REASON: " + stepResult.getTestFailure().getMessage(), "plain/text", ""));
        }
        String attachType = "image/" + PropertyResolver.getDefaultScreenshotFormat().toLowerCase();
        for (Screenshot screenshot : screenshots) {
            attachments.add(new JSONAttachment(screenshot.getScreenshotFile().getName(), attachType, screenshot.getScreenshotFile().getAbsolutePath()));
            if (screenshot.hasPageFile()) {
                attachments.add(new JSONAttachment(screenshot.getPageFile().getName(), "text/html", screenshot.getPageFile().getAbsolutePath()));
            }
        }
    }
}