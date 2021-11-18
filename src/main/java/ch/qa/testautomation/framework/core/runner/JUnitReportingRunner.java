package ch.qa.testautomation.framework.core.runner;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.core.component.PerformableTestCases;
import ch.qa.testautomation.framework.core.component.TestCaseObject;
import ch.qa.testautomation.framework.core.component.TestStepMonitor;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;

public class JUnitReportingRunner extends BlockJUnit4ClassRunner {
    private Description rootDescription;
    private int numberOfTestCases;
    private PerformableTestCases allTestCases;
    private LinkedHashMap<String, Description> allDescriptions = new LinkedHashMap<>();

    /**
     * Creates a BlockJUnit4ClassRunner to run {@code PerformableTestCases}
     *
     * @param testCasesClass test case aggregation
     * @throws InitializationError if the test class is malformed.
     */
    public JUnitReportingRunner(Class<? extends PerformableTestCases> testCasesClass) throws InitializationError, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        super(testCasesClass);
        allTestCases = testCasesClass.getDeclaredConstructor().newInstance();
        if (allTestCases.getTestCases().isEmpty() && allTestCases.getSequenceCaseRunners().isEmpty()) {
            SystemLogger.warn("No Test Cases was found to run! System exit ...");
            System.exit(-1);
        } else {
            numberOfTestCases = allTestCases.countTestCases();
            rootDescription = Description.createSuiteDescription(testCasesClass);
            for (TestCaseObject testCaseObject : allTestCases.getTestCases()) {
                String toid = testCaseObject.getObjectId();
                Description tcDescription = Description.createSuiteDescription(toid);
                allDescriptions.put(toid, tcDescription);
                testCaseObject.getSteps().forEach(testCaseStep -> {
                    Description stepDescription = Description.createTestDescription(toid, testCaseStep.getName());
                    tcDescription.addChild(stepDescription);
                    allDescriptions.put(toid + testCaseStep.getName(), stepDescription);
                });
                rootDescription.addChild(tcDescription);
            }
        }
    }

    @Override
    public Description getDescription() {
        return rootDescription;
    }

    @Override
    public int testCount() {
        return numberOfTestCases;
    }

    /**
     * Returns a {@link Statement}: Call
     * on each object returned by {@link #getChildren()} (subject to any imposed
     * filter and sort)
     */
    @Override
    protected Statement childrenInvoker(final RunNotifier notifier) {
        return new Statement() {
            @Override
            public void evaluate() {
                TestStepMonitor testStepMonitor = new TestStepMonitor(notifier, getDescription(), testCount(), allDescriptions);
                try {
                    allTestCases.useTestStepMonitor(testStepMonitor).run();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
