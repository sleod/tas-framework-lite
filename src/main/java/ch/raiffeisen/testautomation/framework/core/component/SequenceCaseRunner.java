package ch.raiffeisen.testautomation.framework.core.component;


import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class SequenceCaseRunner implements Runnable {

    private final List<TestCaseObject> testCaseObjects;

    public SequenceCaseRunner(List<TestCaseObject> testCaseObjects) {
        if (testCaseObjects != null && !testCaseObjects.isEmpty()) {
            this.testCaseObjects = testCaseObjects;
        } else {
            this.testCaseObjects = new LinkedList<>();
        }
    }

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

    @Override
    public void run() {
        testCaseObjects.forEach(TestCaseObject::run);
    }


    public List<TestCaseObject> getAllCases() {
        Collections.sort(testCaseObjects);
        return testCaseObjects;
    }
}