package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.log;

/**
 * TestStep Monitor for each testcase
 */
public class TestStepMonitor {

    private final Description rootDescription; //test suites
    private final int totalTestCases;
    private int testCounter = 0;
    private final RunNotifier notifier;
    private final HashMap<String, Description> currentTestCases = new HashMap<>(PropertyResolver.getExecutionThreads());
    private final HashMap<String, Description> lastSteps = new HashMap<>(PropertyResolver.getExecutionThreads());
    private final Map<String, Description> allDescriptions;
    private final HashMap<String, Boolean> skip = new HashMap<>(PropertyResolver.getExecutionThreads());

    /**
     * construct test step monitor with junit notifier and description
     *
     * @param notifier        junit notifier
     * @param rootDescription root description
     * @param totalTestCases  number of test cases
     */
    public TestStepMonitor(RunNotifier notifier, Description rootDescription, int totalTestCases, Map<String, Description> allDescriptions) {
        this.notifier = notifier;
        this.totalTestCases = totalTestCases;
        this.rootDescription = rootDescription;
        this.allDescriptions = allDescriptions;
    }

    public Description getLastStep() {
        return lastSteps.get(getTid());
    }

    public Description getCurrentTestCase() {
        return currentTestCases.get(getTid());
    }

    /**
     * prepare test steps for the junit runner before test start
     *
     * @param testName test name
     */
    public synchronized void beforeTest(String testName) {
        if (testCounter == 0) {
            notifier.fireTestRunStarted(rootDescription);
        }
        currentTestCases.put(getTid(), allDescriptions.get(testName));
        skip.put(getTid(), false);
    }

    /**
     * do something after test execution
     */
    public synchronized void afterTest() {
        testCounter++;
        if (testCounter == totalTestCases) {
            notifier.fireTestRunFinished(new Result());
        }
    }

    /**
     * do something before step
     *
     * @param step       step name
     * @param stepResult step result
     */
    public synchronized void beforeStep(String step, TestStepResult stepResult) {
        SystemLogger.setCurrTestStepResult(Thread.currentThread().getName(), stepResult);
        if (!skip.get(getTid())) {
            notifier.fireTestStarted(allDescriptions.get(step));
        }
        SystemLogger.logStepInfo(getTid(), "Step Start: " + step);
    }

    /**
     * fail the test step
     *
     * @param step name of step
     * @param ex   exception
     */
    public synchronized void failed(String step, Throwable ex, boolean stop) {
        SystemLogger.logStepInfo(getTid(), "Step failed: " + step);
        SystemLogger.logStepInfo(getTid(), "Reason: " + ex.getMessage());
        Description currentStep = allDescriptions.get(step);
        notifier.fireTestFailure(new Failure(currentStep, ex));
        notifier.fireTestFinished(currentStep);
        lastSteps.put(getTid(), currentStep);
        skip.put(getTid(), stop);
    }

    /**
     * break the test step because of known issue
     *
     * @param step name of step
     * @param ex   exception
     */
    public synchronized void broken(String step, Throwable ex) {
        SystemLogger.log("WARN", "Step Broken: {} (cause: {})", step, ex.getMessage());
        SystemLogger.logStepInfo(getTid(), "Step broken: " + step);
        SystemLogger.logStepInfo(getTid(), "Reason: " + ex.getMessage());
        Description currentStep = allDescriptions.get(step);
        notifier.fireTestFailure(new Failure(currentStep, ex));
        notifier.fireTestFinished(currentStep);
        lastSteps.put(getTid(), currentStep);
        skip.put(getTid(), false);
    }


    /**
     * succeed test step
     *
     * @param step name of step
     */
    public synchronized void succeed(String step) {
        Description currentStep = allDescriptions.get(step);
        SystemLogger.logStepInfo(getTid(), "Step Successful: " + step);
        notifier.fireTestFinished(currentStep);
        lastSteps.put(getTid(), currentStep);
        skip.put(getTid(), false);
    }

    public synchronized void ignorable(String step) {
        Description currentStep = allDescriptions.get(step);
        SystemLogger.logStepInfo(getTid(), "Step Ignored: " + step);
        notifier.fireTestIgnored(currentStep);
        lastSteps.put(getTid(), currentStep);
        skip.put(getTid(), true);
    }

    /**
     * build step into with simple pattern
     *
     * @param method method to be invoked
     * @param args   arguments
     */
    @SuppressWarnings("unchecked")
    public synchronized void stepInfo(Method method, Object... args) {
        String comment = "Invoke Method: " + method.getName();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(comment);
        Class<?>[] types = method.getParameterTypes();
        if (types.length > 0) {
            stringBuilder.append(" With Parameter: ");
            if (args.length == 1 && (args[0] instanceof Map)) {
                ((Map) args[0]).forEach((key, value) -> stringBuilder.append("\n").append(key).append(" -> ").append(value));
            } else {
                for (Object arg : args) {
                    stringBuilder.append(" | ").append(arg);
                }
                stringBuilder.append(" |");
            }
        } else {
            stringBuilder.append(" Without Parameter.");
        }
        SystemLogger.logStepInfo(getTid(), stringBuilder.toString());
    }

    private String getTid() {
        return Thread.currentThread().getName();
    }
}
