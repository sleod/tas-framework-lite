package ch.qa.testautomation.tas.core.component;

import ch.qa.testautomation.tas.configuration.TASConfiguration;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.report.ReportBuilder;
import ch.qa.testautomation.tas.core.report.allure.ReportBuilderAllureService;
import com.codeborne.selenide.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.fatal;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.tas.configuration.PropertyResolver.*;
import static ch.qa.testautomation.tas.core.component.TestRunManager.*;
import static java.util.Arrays.asList;

public abstract class PerformableTestCases {

    private List<TestCaseObject> testCaseObjects = Collections.emptyList();

    private final TASConfiguration configuration = new TASConfiguration();

    public PerformableTestCases() {
        try {
            setUpFramework();
            retrieveResources();
            healthCheck();//health check
            setPerformer(this);
            cleanResultsByPresentOnServer();
            testCaseObjects = initTestCases(findAllFilePathOfTestCaseFile(includeFilePatterns(), excludeFilePatterns()), getMetaFilters());
            loadGlobalTestData();
            setUpSelenide();
            info("Test Run contains Test Cases: " + testCaseObjects.size());
        } catch (Throwable throwable) {
            fatal(throwable);
        }
    }

    public List<TestCaseObject> getTestCaseObjects() {
        return testCaseObjects;
    }

    /**
     * standard BeforeClass that will be used by jUnit framework
     */
    @BeforeAll
    public static void beforeTests() {
        info("Starting Test Run...");
        //static files for report and start count run number
        ReportBuilder.generateExecutorJSON();
        ReportBuilder.generateEnvironmentProperties();
    }

    public Stream<DynamicContainer> getSingleTestCases() {
        //single test cases
        return testCaseObjects.stream()
                .filter(testCaseObject -> !isValid(testCaseObject.getSeriesNumber()))
                .map(this::getTestCaseReady);
    }

    public Stream<DynamicContainer> getSeriesTestCases() {
        Map<String, TestCaseObject> serienTestCases = new TreeMap<>();
        testCaseObjects.stream().filter(testCaseObject -> isValid(testCaseObject.getSeriesNumber()))
                .forEach(testCaseObject -> {
                    String seriesNr = testCaseObject.getSeriesNumber();
                    if (serienTestCases.containsKey(seriesNr)) {
                        seriesNr += ".1";
                    }
                    serienTestCases.put(seriesNr, testCaseObject);
                });
        return serienTestCases.values().stream().map(this::getTestCaseReady);
    }

    public DynamicContainer getTestCaseReady(TestCaseObject testCaseObject) {
        if (isValid(testCaseObject.getSeriesNumber())) {
            testCaseObject.setName(testCaseObject.getName() + " - SN - " + testCaseObject.getSeriesNumber());
        }
        return DynamicContainer.dynamicContainer(testCaseObject.prepareAndGetDisplayName(), testCaseObject.getTestSteps());
    }

    @TestFactory
    @DisplayName("Execute Test Cases...")
    public Stream<DynamicContainer> runTCs() {
        return Stream.concat(getSeriesTestCases(), getSingleTestCases());
    }

    /**
     * closing test run, close drivers and generate reports
     */
    @AfterAll
    public static void afterTests() {
        info("Ending Test Run...");
        //finish afterTest for last test case
        TestStepMonitor.afterAllSteps();
        //generate allure html report locally, only wenn not parallel execution
        if (!PropertyResolver.isExecutionRemoteParallelEnabled()) {
            info("Generate local Allure and XML Report.");
            new ReportBuilder().generateReports();
            //generate allure report on server
            if (PropertyResolver.isAllureReportServiceEnabled()) {
                info("Upload Allure Extra Files to Server.");
                new ReportBuilderAllureService().uploadAllureExtra();
                info("Generate Allure Report on Server.");
                new ReportBuilderAllureService().generateReportOnService();
            }
            //clean up
            DriverManager.cleanUp();
        }
        info("Test Run is finished.");
    }

    public TASConfiguration getTASConfiguration() {
        return configuration;
    }

    /**
     * Override to define which patterns of test cases to run.
     * Can be overridden by subclasses to specify test case file names
     * <p>
     * Default includes all test case files in base dir and all subdirectories.
     *
     * @return the patterns of test case files to include
     */
    protected List<String> includeFilePatterns() {
        return asList("*" + getTestCaseFileExtension(), "**/*" + getTestCaseFileExtension());
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
        return getMetaFilter();
    }

    /**
     * override to set up selenide
     */
    protected void setUpSelenide() {
        Configuration.reportsFolder = StringUtils.chop(PropertyResolver.getTestCaseReportLocation());
        Configuration.timeout = PropertyResolver.getDriverWaitTimeout();
        Configuration.savePageSource = false;
        Configuration.screenshots = false;
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

    /**
     * Set up Framework using FrameworkConfiguration
     */
    protected void setUpFramework() {
        getTASConfiguration().setTestCaseFileExtension(".tas");
    }

    private void cleanResultsByPresentOnServer() {
        if (PropertyResolver.isCleanUpResults() && PropertyResolver.isAllureReportServiceEnabled() && !PropertyResolver.isExecutionRemoteParallelEnabled()) {
            info("Clean Allure Results on Server if present.");
            new ReportBuilderAllureService().cleanResultsByPresent();
        }
    }
}