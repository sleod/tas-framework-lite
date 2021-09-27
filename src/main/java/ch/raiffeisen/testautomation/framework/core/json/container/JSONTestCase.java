package ch.raiffeisen.testautomation.framework.core.json.container;

import ch.raiffeisen.testautomation.framework.core.json.customDeserializer.CustomStepListDeserializer;
import ch.raiffeisen.testautomation.framework.core.json.customDeserializer.CustomStringListDeserializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Object Container Class of JSON Test Case
 */
public class JSONTestCase extends JSONContainer {
    @JsonProperty
    private String name;
    @JsonProperty
    private List<String> meta;
    @JsonProperty
    private String description;
    @JsonProperty
    private String source;
    @JsonProperty
    private String testCaseId;
    @JsonProperty
    private String requirement;
    @JsonProperty
    private String reference;
    @JsonProperty
    private String screenshotLevel;
    @JsonProperty
    private String testDataRef;
    @JsonProperty
    private String type;
    @JsonProperty
    private String startURL;
    @JsonProperty
    private String appName;
    @JsonProperty
    private String activity;
    @JsonProperty
    private String seriesNumber;
    @JsonProperty
    private List<JSONTestCaseStep> steps;
    @JsonProperty
    private String additionalTestDataFile;
    @JsonProperty
    private String story;
    @JsonProperty
    private String epic;
    @JsonProperty
    private String feature;

    @JsonIgnore
    private String parentFolderName;

    @JsonIgnore
    private String fileName;

    @JsonIgnore
    private List<String> coverage = Collections.emptyList();

    public String getAdditionalTestDataFile() {
        return additionalTestDataFile;
    }

    public void setAdditionalTestDataFile(String additionalTestDataFile) {
        this.additionalTestDataFile = additionalTestDataFile;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public String getRequirement() {
        return requirement;
    }

    public void setRequirement(String requirement) {
        this.requirement = requirement;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getScreenshotLevel() {
        return screenshotLevel;
    }

    public void setScreenshotLevel(String screenshotLevel) {
        this.screenshotLevel = screenshotLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMeta() {
        return meta;
    }

    @JsonDeserialize(using = CustomStringListDeserializer.class)
    public void setMeta(List<String> metaTags) {
        this.meta = metaTags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<JSONTestCaseStep> getSteps() {
        return steps;
    }

    @JsonDeserialize(using = CustomStepListDeserializer.class)
    public void setSteps(List<JSONTestCaseStep> steps) {
        this.steps = steps;
    }

    public List<String> getTestObjectNames() {
        List<String> names = new LinkedList<>();
        for (JSONTestCaseStep jsonTestCaseStep : getSteps()) {
            if (!names.contains(jsonTestCaseStep.getTestObject()))
                names.add(jsonTestCaseStep.getTestObject());
        }
        return names;
    }

    public void setTestDataRef(String testDataRef) {
        this.testDataRef = testDataRef;
    }

    public String getTestDataRef() {
        return testDataRef;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartURL() {
        return startURL;
    }

    public void setStartURL(String startURL) {
        this.startURL = startURL;
    }

    public String getPackage() {
        return parentFolderName;
    }

    public void setPackage(String parentFolderName) {
        this.parentFolderName = parentFolderName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getCoverage() {
        return coverage;
    }

    public void setCoverage(List<String> coverage) {
        this.coverage = coverage;
    }

    public String getSeriesNumber() {
        return seriesNumber;
    }

    public void setSeriesNumber(String seriesNumber) {
        this.seriesNumber = seriesNumber;
    }

    public String getStory() {
        return story;
    }

    public void setStory(String story) {
        this.story = story;
    }

    public String getEpic() {
        return epic;
    }

    public void setEpic(String epic) {
        this.epic = epic;
    }

    public String getFeature() {
        return feature;
    }

    public void setFeature(String feature) {
        this.feature = feature;
    }

    public void addCoverage(String testCaseId) {
        if (coverage.isEmpty()) {
            coverage = new LinkedList<>();
        }
        if (!coverage.contains(testCaseId)) {
            coverage.add(testCaseId);
        }
    }
}
