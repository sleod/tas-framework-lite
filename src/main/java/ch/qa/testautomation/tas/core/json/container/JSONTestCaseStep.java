package ch.qa.testautomation.tas.core.json.container;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * Object Container Class of JSON Test Case
 */
@Setter
@Getter
public class JSONTestCaseStep extends JSONContainer {

    private String name;
    private String testObject;
    private String using;
    private String takeScreenshot;
    private String stopOnError;
    private String comment;
    private int retry = 0;

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
