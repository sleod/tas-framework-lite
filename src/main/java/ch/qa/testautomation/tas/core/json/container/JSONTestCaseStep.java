package ch.qa.testautomation.tas.core.json.container;

import java.util.Objects;

/**
 * Object Container Class of JSON Test Case
 */
public class JSONTestCaseStep extends JSONContainer {

    private String name;
    private String testObject;
    private String using;
    private String takeScreenshot;
    private String stopOnError;
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

    @Override
    public boolean equals(Object target) {
        // self check
        if (this == target) return true;
        // null check
        if (target == null) return false;
        // type check and cast
        if (getClass() != target.getClass()) return false;
        JSONTestCaseStep other = (JSONTestCaseStep) target;
        // field comparison
        return Objects.equals(name, other.name)
                && Objects.equals(testObject, other.testObject)
                && Objects.equals(using, other.using)
                && Objects.equals(comment, other.comment);
    }

    // https://mkyong.com/java/java-how-to-overrides-equals-and-hashcode/
    @Override
    public int hashCode() {
        return Objects.hash(name, testObject, using, comment);
    }
}
