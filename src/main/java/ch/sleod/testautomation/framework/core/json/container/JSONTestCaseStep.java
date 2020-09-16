package ch.sleod.testautomation.framework.core.json.container;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object Container Class of JSON Test Case
 */
public class JSONTestCaseStep {

    @JsonProperty
    private String name;
    @JsonProperty
    private String testObject;
    @JsonProperty
    private String using;

    public String getTestObject() {
        return testObject;
    }

    public void setTestObject(String testObject) {
        this.testObject = testObject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsing(String using) {
        this.using = using;
    }

    public String getUsing() {
        return using;
    }
}
