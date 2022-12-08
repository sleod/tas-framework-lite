package ch.qa.testautomation.framework.core.component;


import org.junit.jupiter.api.DynamicContainer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class SequenceCaseRunner {

    private final List<TestCaseObject> testCaseObjects;

    public SequenceCaseRunner() {
        this.testCaseObjects = new LinkedList<>();
    }

    public void addTestCase(TestCaseObject testCaseObject) {
        if (!testCaseObjects.contains(testCaseObject)) {
            testCaseObjects.add(testCaseObject);
        }
    }

    public int size() {
        return testCaseObjects.size();
    }

    public Stream<DynamicContainer> getAllCases() {
        Collections.sort(testCaseObjects);
        return testCaseObjects.stream().map(testCaseObject -> {
            testCaseObject.setName(testCaseObject.getName() + " - with Order - " + testCaseObject.getSeriesNumber());
            return DynamicContainer.dynamicContainer(testCaseObject.prepareAndGetDisplayName(), testCaseObject.getTestSteps());
        });
    }
}