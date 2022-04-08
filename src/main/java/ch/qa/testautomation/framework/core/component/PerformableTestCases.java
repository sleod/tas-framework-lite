package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.core.report.ReportBuilder;
import ch.qa.testautomation.framework.core.runner.JUnitReportingRunner;
import com.codeborne.selenide.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;
import static ch.qa.testautomation.framework.core.report.ReportBuilder.generateAllureHTMLReport;
import static ch.qa.testautomation.framework.core.report.ReportBuilder.generateMavenTestXMLReport;

@RunWith(JUnitReportingRunner.class)
public abstract class PerformableTestCases {

    private static List<TestCaseObject> testCaseObjects;
    private static Map<String, SequenceCaseRunner> sequenceCaseRunners;

    public PerformableTestCases() {
        try {
            TestRunManager.retrieveResources(getTestRunPropertiesPath());
            TestRunManager.setPerformer(this);
            TestRunManager.cleanResultsByPresent();
            TestRunManager.initTestCases(TestRunManager.filePaths(getFileBaseDir(), includeFilePatterns(), excludeFilePatterns()), getMetaFilters());
            sortSequenceTestCases();
            TestRunManager.loadGlobalTestData();
            setUpSelenide();
        } catch (Throwable throwable) {
            error(throwable);
            info("Test Case Initialization failed. Exit.. Please check the base dir of files and package of implementation of Test Object.");
        }
    }

    protected void sortSequenceTestCases() {
        if (sequenceCaseRunners != null && !sequenceCaseRunners.isEmpty()) {
            int seqNumber = 0;
            for (Map.Entry<String, SequenceCaseRunner> entry : sequenceCaseRunners.entrySet()) {
                SequenceCaseRunner value = entry.getValue();
                List<TestCaseObject> valueList = value.getAllCases();
                seqNumber++;
                for (int i = 0; i < valueList.size(); i++) {
                    TestCaseObject testCaseObject = valueList.get(i);
                    testCaseObject.setName(testCaseObject.getTestCase().getName() + " - Sequence " + seqNumber + " variante " + (i + 1));
                }
            }
        }
    }

    /**
     * standard BeforeClass that will be used by jUnit framework
     */
    @BeforeClass
    public static void beforeTests() {
        info("Starting Test Run...");
        //environment.properties file for report
        ReportBuilder.generateEnvironmentProperties();
        //executor.json file for report
        JSONContainerFactory.generateExecutorJSON();
    }

    /**
     * the main entrance of test run which using junit @Test annotation
     */
    @Test
    public void run() {
        if (!PropertyResolver.isMultiThreadingEnabled()) {
            //first served: single cases
            testCaseObjects.stream().filter(testCaseObject -> testCaseObject.getSeriesNumber() == null
                    || testCaseObject.getSeriesNumber().isEmpty()).forEach(TestCaseObject::run);
            //sequenced cases
            if (!getSequenceCaseRunners().isEmpty()) {
                getSequenceCaseRunners().values().forEach(SequenceCaseRunner::run);
            }
        } else {
            ExecutorService executor = Executors.newFixedThreadPool(PropertyResolver.getExecutionThreads());
            try {
                //first served: single cases
                testCaseObjects.stream().filter(testCaseObject -> testCaseObject.getSeriesNumber() == null
                        || testCaseObject.getSeriesNumber().isEmpty()).forEach(executor::submit);
                //sequenced cases
                if (!getSequenceCaseRunners().isEmpty()) {
                    getSequenceCaseRunners().values().forEach(executor::submit);
                }
                executor.awaitTermination(10, TimeUnit.SECONDS);
                executor.shutdown();
                while (!executor.isTerminated()) {
                    executor.awaitTermination(5, TimeUnit.SECONDS);
                }
            } catch (InterruptedException ex) {
                error(ex);
                warn("Execution of tasks interrupted!");
            } finally {
                if (!executor.isTerminated()) {
                    warn("Non-finished tasks will be cancelled!");
                }
                executor.shutdownNow();
                trace("Execution service shutdown finished");
            }
        }
    }

    /**
     * count test cases
     *
     * @return number of test cases
     */
    public int countTestCases() {
        return getTestCases().size();
    }

    //getter
    public List<TestCaseObject> getTestCases() {
        if (testCaseObjects == null) {
            return Collections.emptyList();
        }
        return testCaseObjects;
    }

    public void setTestCaseObjects(List<TestCaseObject> testCaseObjects) {
        PerformableTestCases.testCaseObjects = testCaseObjects;
    }

    public Map<String, SequenceCaseRunner> getSequenceCaseRunners() {
        if (sequenceCaseRunners == null) {
            sequenceCaseRunners = new LinkedHashMap<>();
        }
        return sequenceCaseRunners;
    }

    public void addSequenceCaseRunner(String key, TestCaseObject testCaseObject) {
        trace("Add sequenced test case with key: " + key);
        getSequenceCaseRunners().putIfAbsent(key, new SequenceCaseRunner());
        getSequenceCaseRunners().get(key).addTestCase(testCaseObject);
    }

    /**
     * method for inject test step monitor
     *
     * @param testStepMonitor test step monitor
     * @return this object
     */
    public PerformableTestCases useTestStepMonitor(TestStepMonitor testStepMonitor) {
        testCaseObjects.forEach(testCaseObject -> testCaseObject.useTestStepMonitor(testStepMonitor));
        return this;
    }

    /**
     * closing test run, close drivers and generate reports
     */
    @AfterClass
    public static void afterTests() {
        endingTests();
    }

    /**
     * closing test run, close drivers and generate reports
     */

    protected static void endingTests() {
        DriverManager.closeDriver();
        if (!testCaseObjects.isEmpty() && !testCaseObjects.get(0).getTestRunResult().getStepResults().isEmpty()) {
            //generate allure html report on server
            TestRunManager.generateReportOnService();
            //generate allure html report locally
            generateAllureHTMLReport();
            generateMavenTestXMLReport(testCaseObjects);
        } else {
            throw new RuntimeException("No test case found with meta filter or not selected!");
        }
        info("Ending Test Run...");
    }

    /**
     * Define the test case file format
     * Can be overridden by subclasses
     *
     * @return file format suffix
     */
    protected String getTestCaseFileFormat() {
        return ".tas";
    }

    /**
     * Define the relative path from project's resources root directory to the directory where the test case files should be searched for inside.
     * Can be overridden by subclasses to use different base directory.
     * <p>
     *
     * @return the relative base path, is not allowed to startNow with "/" and not to end with "/".
     */
    protected String getFileBaseDir() {
        return "testCases";
    }

    /**
     * Override to define which patterns of test cases to run.
     * Can be overridden by subclasses to specify test case file names
     * <p>
     * Default includes all test case files in base dir and all sub directories.
     *
     * @return the patterns of test case files to include
     */
    protected List<String> includeFilePatterns() {
        return Collections.emptyList();
    }

    /**
     * Override to filter out some test cases not to run via pattern of file name.
     * Can be overridden by subclasses to specify test case file names
     * <p>
     * Default does not filter any test cases.
     *
     * @return the patterns of test case files to exclude
     */
    protected List<String> excludeFilePatterns() {
        return Collections.emptyList();
    }

    /**
     * Override to filter out some test cases not to run via tag in test case
     * Can be overridden by subclasses to specify test case tags
     * <p>
     * Default does not filter any test cases.
     *
     * @return the patterns of test case files to exclude
     */
    protected List<String> getMetaFilters() {
        return Collections.emptyList();
    }

    /**
     * override to relocate the property file
     *
     * @return path of property file
     */
    protected String getTestRunPropertiesPath() {
        return "properties/DefaultTestRunProperties.properties";
    }

    /**
     * override to set up selenide
     */
    protected void setUpSelenide() {
        Configuration.reportsFolder = StringUtils.chop(PropertyResolver.getDefaultTestCaseReportLocation());
        Configuration.screenshots = false;
        Configuration.timeout = PropertyResolver.getSelenideTimeout();
        Configuration.savePageSource = false;
    }

    /**
     * override to set up filter for CSV test data selection
     *
     * @return map of filter : <column, value>
     */
    protected Map<String, String> getCSVTestDataSelectionFilter() {
        return Collections.emptyMap();
    }

    /**
     * override to set up filter for CSV test data exclusion
     *
     * @return map of filter : <column, value>
     */
    protected Map<String, String> getCSVTestDataExclusionFilter() {
        return Collections.emptyMap();
    }
}