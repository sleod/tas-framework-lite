package ch.qa.testautomation.tas.core.json.container;

import ch.qa.testautomation.tas.core.json.customDeserializer.CustomStepListDeserializer;
import ch.qa.testautomation.tas.core.json.customDeserializer.CustomStringListDeserializer;
import ch.qa.testautomation.tas.exception.ApollonBaseException;
import ch.qa.testautomation.tas.exception.ApollonErrorKeys;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Object Container Class of JSON Test Case
 */
public class JSONTestCase extends JSONContainer {

    private String name;
    private List<String> meta;
    private String description;
    private String source;
    private String testCaseId;
    private Map<String, String> testCaseIdMap;
    private String requirement;
    private String reference;
    private String screenshotLevel;
    private String testDataRef;
    private String type;
    private String startURL;
    private String appName;//only for app installation with file
    private String activity;
    private String appPackage;
    private String bundleId;
    private String seriesNumber;
    private List<JSONTestCaseStep> steps;
    private String additionalTestDataFile;
    private String story;
    private String epic;
    private String feature;

    public String getAdditionalTestDataFile() {
        return additionalTestDataFile;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
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
        return Objects.isNull(testCaseId) ? "" : testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public Map<String, String> getTestCaseIdMap() {
        return Objects.isNull(testCaseIdMap) ? Collections.emptyMap() : testCaseIdMap;
    }

    @JsonAnySetter
    public void setTestCaseIdMap(String key, String value) {
        if (Objects.isNull(testCaseIdMap)) {
            testCaseIdMap = new HashMap<>(2);
        }
        testCaseIdMap.put(key, value);
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
        this.name = name.trim();
        if (name.isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, "Name of Test Case is empty!");
        }
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

    @Override
    public boolean equals(Object target) {
        // self check
        if (this == target) return true;
        // null check
        if (Objects.isNull(target)) return false;
        // type check and cast
        if (getClass() != target.getClass()) return false;
        JSONTestCase other = (JSONTestCase) target;
        // field comparison
        return Objects.equals(name, other.name)
                && Objects.equals(description, other.description)
                && Objects.equals(testDataRef, other.testDataRef)
                && CollectionUtils.isEqualCollection(steps, other.steps)
                && Objects.equals(additionalTestDataFile, other.additionalTestDataFile);
    }

    // https://mkyong.com/java/java-how-to-overrides-equals-and-hashcode/
    @Override
    public int hashCode() {
        return Objects.hash(name,
                description,
                testDataRef,
                steps,
                additionalTestDataFile);
    }

}
