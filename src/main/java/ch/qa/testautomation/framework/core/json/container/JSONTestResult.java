package ch.qa.testautomation.framework.core.json.container;

import ch.qa.testautomation.framework.core.component.TestRunResult;
import ch.qa.testautomation.framework.core.json.customDeserializer.CustomAttachmentListDeserializer;
import ch.qa.testautomation.framework.core.json.customDeserializer.CustomResultLabelDserializer;
import ch.qa.testautomation.framework.core.json.customDeserializer.CustomStepResultListDeserializer;
import ch.qa.testautomation.framework.core.json.customDeserializer.CustomStringListDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.*;

public class JSONTestResult {
    @JsonProperty
    private String name;
    @JsonProperty
    private String fullName;
    @JsonProperty
    private String description;
    @JsonProperty
    private String status;
    @JsonProperty
    private String uuid;
    @JsonProperty
    private long start;
    @JsonProperty
    private long stop;
    @JsonProperty
    private String stage;
    @JsonProperty
    private String historyId;
    @JsonProperty
    private List<JSONResultLabel> labels = new LinkedList<>();
    @JsonProperty
    private List<JSONStepResult> steps = new LinkedList<>();
    @JsonProperty
    private List<String> links = new LinkedList<>();
    @JsonProperty
    private Map<String, Object> statusDetails = new LinkedHashMap<>();
    @JsonProperty
    private List<JSONAttachment> attachments = new LinkedList<>();

    public JSONTestResult(String name, String fullName, String description, String status, long start, long stop, String stage, String historyId) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.status = status;
        this.uuid = UUID.randomUUID().toString();
        this.start = start;
        this.stop = stop;
        this.stage = stage;
        this.historyId = historyId;
    }

    public JSONTestResult() {
    }

    /**
     * fullName and history id need be set later
     *
     * @param testRunResult test run result
     */
    public JSONTestResult(TestRunResult testRunResult) {
        this.name = testRunResult.getName();
        this.description = testRunResult.getDescription();
        this.status = testRunResult.getStatus().text();
        this.uuid = UUID.randomUUID().toString();
        this.start = testRunResult.getStart();
        this.stop = testRunResult.getStop();
        this.stage = "finished";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
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

    public String getHistoryId() {
        return historyId;
    }

    public void setHistoryId(String historyId) {
        this.historyId = historyId;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    @JsonDeserialize(using = CustomResultLabelDserializer.class)
    public void setLabels(List<JSONResultLabel> labels) {
        this.labels = labels;
    }

    public void updateLabel(String name, String value) {
        for (JSONResultLabel jsonResultLabel : labels) {
            if (jsonResultLabel.getName().equalsIgnoreCase(name)) {
                jsonResultLabel.setValue(value);
                break;
            }
        }
    }

    public void addLabel(String name, String value) {
        labels.add(new JSONResultLabel(name, value));
    }

    public List<JSONStepResult> getSteps() {
        return steps;
    }

    @JsonDeserialize(using = CustomStepResultListDeserializer.class)
    public void setSteps(List<JSONStepResult> steps) {
        this.steps = steps;
    }

    public List<String> getLinks() {
        return links;
    }

    @JsonDeserialize(using = CustomStringListDeserializer.class)
    public void setLinks(List<String> links) {
        this.links = links;
    }

    public Map<String, Object> getStatusDetails() {
        return statusDetails;
    }

    @JsonAnySetter
    public void setStatusDetails(String key, Object value) {
        this.statusDetails.put(key, value);
    }

    public List<JSONAttachment> getAttachments() {
        return attachments;
    }

    @JsonDeserialize(using = CustomAttachmentListDeserializer.class)
    public void setAttachments(List<JSONAttachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(String name, String type, String source) {
        attachments.add(new JSONAttachment(name, type, source));
    }

    public void addStep(JSONStepResult stepResult) {
        steps.add(stepResult);
    }

    @JsonIgnore
    public String getSuite() {
        String suite = "";
        for (JSONResultLabel label : labels) {
            if (label.getName().equalsIgnoreCase("suite")) {
                suite = label.getValue();
                break;
            }
        }
        return suite;
    }
}
