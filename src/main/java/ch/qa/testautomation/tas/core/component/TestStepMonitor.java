package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.common.enumerations.TestStatus;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.TestAbortedException;

import java.util.HashMap;
import java.util.Map;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.error;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;

/**
 * TestStep Monitor for each testcase
 */
public class TestStepMonitor {

    private static final Map<String, TestCaseObject> currentTests = new HashMap<>();
    private static final Map<String, TestCaseStep> currentSteps = new HashMap<>();
    private static final Map<String, Boolean> isStopMap = new HashMap<>();

    public static boolean isStop() {
        return isStopMap.get(Thread.currentThread().getName());
    }

    public static void setIsStop(boolean isStop) {
        isStopMap.put(Thread.currentThread().getName(), isStop);
    }

    public static void setCurrentStep(TestCaseStep currentStep) {
        currentSteps.put(Thread.currentThread().getName(), currentStep);
    }

    public static void setCurrentTest(TestCaseObject currentTest) {
        afterAllSteps();
        currentTests.put(Thread.currentThread().getName(), currentTest);
        beforeAllSteps();
    }

    public static TestCaseStep getCurrentStep() {
        return currentSteps.get(Thread.currentThread().getName());
    }

    public static TestCaseObject getCurrentTest() {
        return currentTests.get(Thread.currentThread().getName());
    }

    public static String getCurrentTestStepName() {
        return getCurrentStep().getName();
    }

    public static String getCurrentTestCaseName() {
        return getCurrentTest().getName();
    }

    public static void beforeAllSteps() {
        if (getCurrentTest() != null) {
            info("Finish Pre-Process of Test Case: " + getCurrentTest().getName());
            getCurrentTest().beforeTest();
            setIsStop(false);
        }
    }

    public static void afterAllSteps() {
        if (getCurrentTest() != null) {
            info("Finish Post-Process of Test Case: " + getCurrentTest().getName());
            try {
                getCurrentTest().afterTest();
            } catch (Throwable ex) {
                error(new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Post Process of Test Case can not be executed!"));
            }
        }
    }

    public static void processResult(TestStepResult testStepResult) {
        if (testStepResult.getStatus().equals(TestStatus.FAIL)) {
            //prepare retry
            if (PropertyResolver.isRetryOnErrorEnabled()) {
                TestRunManager.storeRetryStep(getCurrentTest().getFilePath(), getCurrentTest().getName(), getCurrentStep().getOrderNumber());
                TestRunManager.storeSessions();
            }
            //fail the step for report
            Assertions.fail("Test Failed", testStepResult.getTestFailure().getException());
        } else if (testStepResult.getStatus().equals(TestStatus.SKIPPED)) {
            throw new TestAbortedException("Test Step Aborted because of Stop on Error.");
        }
    }

}
