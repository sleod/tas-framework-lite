package ch.qa.testautomation.framework.core.json.container;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object Container Class of JSON Test Case
 */
public class JSONTestCaseStep extends JSONContainer {

    @JsonProperty
    private String name;
    @JsonProperty
    private String testObject;
    @JsonProperty
    private String using;
    @JsonProperty
    private String takeScreenshot;
    @JsonProperty
    private String stopOnError;
    @JsonProperty
    private String comment;

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

    public String getTakeScreenshot() {
        return takeScreenshot;
    }

    public void setTakeScreenshot(String takeScreenshot) {
        this.takeScreenshot = takeScreenshot;
    }

    public String getStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(String stopOnError) {
        this.stopOnError = stopOnError;
    }
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
