package io.github.sleod.tas.core.json.container;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.sleod.tas.common.enumerations.ScreenshotLevel;
import io.github.sleod.tas.core.json.customDeserializer.CustomStepListDeserializer;
import io.github.sleod.tas.core.json.customDeserializer.CustomStringListDeserializer;
import io.github.sleod.tas.core.json.customDeserializer.CustomTestCaseConditionDeserializer;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * Object Container Class of JSON Test Case
 */
public class JSONTestCase extends JSONContainer {

    @Getter
    private String name;
    @Getter
    private List<String> meta;
    @Setter
    @Getter
    private String description;
    @Setter
    @Getter
    private String source;
    @Setter
    private String testCaseId;
    private Map<String, String> testCaseIdMap;
    @Setter
    @Getter
    private String requirement;
    @Setter
    @Getter
    private String reference;
    @Setter
    @Getter
    private ScreenshotLevel screenshotLevel;
    @Getter
    @Setter
    private String testDataRef;
    @Setter
    @Getter
    private String type;
    @Setter
    @Getter
    private String startURL;
    @Setter
    @Getter
    private String seriesNumber;
    @Getter
    private List<JSONTestCaseStep> steps;
    @Setter
    @Getter
    private String additionalTestDataFile;
    @Setter
    @Getter
    private String story;
    @Setter
    @Getter
    private String epic;
    @Setter
    @Getter
    private String feature;
    @Getter
    private JSONTestCaseConditions conditions;

    public String getTestCaseId() {
        return Objects.isNull(testCaseId) ? "" : testCaseId;
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

    public void setName(String name) {
        this.name = name.trim();
        if (name.isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Name of Test Case is empty!");
        }
    }

    @JsonDeserialize(using = CustomStringListDeserializer.class)
    public void setMeta(List<String> metaTags) {
        this.meta = metaTags;
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

    @JsonDeserialize(using = CustomTestCaseConditionDeserializer.class)
    public void setConditions(JSONTestCaseConditions conditions) {
        this.conditions = conditions;
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
