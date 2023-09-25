package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.core.component.TestRunResult;
import ch.qa.testautomation.tas.core.json.customDeserializer.CustomAttachmentListDeserializer;
import ch.qa.testautomation.tas.core.json.customDeserializer.CustomResultLabelDeserializer;
import ch.qa.testautomation.tas.core.json.customDeserializer.CustomResultLinkDeserializer;
import ch.qa.testautomation.tas.core.json.customDeserializer.CustomStepResultListDeserializer;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.File;
import java.util.*;

public class JSONTestResult extends JSONContainer {
    private String name;
    private String fullName;
    private String description;
    private String status;
    private String uuid;
    private long start;
    private long stop;
    private String stage;
    private String historyId;
    private List<JSONResultLabel> labels = new LinkedList<>();
    private List<JSONStepResult> steps = new LinkedList<>();
    private List<JSONResultLink> links = new LinkedList<>();
    private Map<String, Object> statusDetails = new LinkedHashMap<>();
    private List<JSONAttachment> attachments = new LinkedList<>();

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

    @JsonDeserialize(using = CustomResultLabelDeserializer.class)
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

    public List<JSONResultLabel> getLabels() {
        return labels;
    }

    @JsonDeserialize(using = CustomStepResultListDeserializer.class)
    public void setSteps(List<JSONStepResult> steps) {
        this.steps = steps;
    }

    public List<JSONResultLink> getLinks() {
        return links;
    }

    @JsonDeserialize(using = CustomResultLinkDeserializer.class)
    public void setLinks(List<JSONResultLink> links) {
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

    public void addAttachment(File file) {
        if (file.exists() && file.isFile()) {
            attachments.add(new JSONAttachment(file.getName(), FileOperation.getMediaTypeOfFile(file.getName()), file.getAbsolutePath()));
        }
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

    public void addLink(String testCaseId, String url) {
        if (Objects.isNull(links)) {
            links = new LinkedList<>();
        }
        links.add(new JSONResultLink(testCaseId, "issue", url));
    }

    public void addLink(String testCaseId, Map<String, String> tfsConfig) {
        if (Objects.isNull(links)) {
            links = new LinkedList<>();
        }
        String url = tfsConfig.get("host") + "/" + tfsConfig.get("organization") + "/"
                + tfsConfig.get("collection") + "/" + tfsConfig.get("project") + "/_workitems/edit/" + testCaseId;
        links.add(new JSONResultLink(testCaseId, "tms", url));
    }
}
