package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.configuration.ApollonConfiguration;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.core.report.ReportBuilder;
import com.codeborne.selenide.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;
import static ch.qa.testautomation.framework.common.utils.StringTextUtils.isValid;
import static ch.qa.testautomation.framework.configuration.PropertyResolver.*;
import static ch.qa.testautomation.framework.core.component.TestRunManager.*;
import static java.util.Arrays.asList;

public abstract class PerformableTestCases {

    private List<TestCaseObject> testCaseObjects = Collections.emptyList();
    private final ApollonConfiguration configuration = new ApollonConfiguration();

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
            trace("Test Run contains Test Cases: " + testCaseObjects.size());
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
        //executor.json file for report and start count run number
        JSONContainerFactory.generateExecutorJSON();
    }

    @TestFactory
    @DisplayName("Execute Test Cases ...")
    public Stream<DynamicContainer> runTCs() {
        return Stream.concat(getSeriesTestCases(), getSingleTestCases());
    }

    /**
     * the main entrance of test run which using junit @Test annotation
     */
    public Stream<DynamicContainer> getSingleTestCases() {
        //single test cases
        return testCaseObjects.stream()
                .filter(testCaseObject -> !isValid(testCaseObject.getSeriesNumber()))
                .map(this::getTestCaseReady);
    }

    /**
     * the main entrance of test run which using junit @Test annotation
     */
    public Stream<DynamicContainer> getSeriesTestCases() {
        //sequenced test cases
        Map<String, TestCaseObject> serienTestCases = new TreeMap<>();
        testCaseObjects.stream().filter(testCaseObject -> isValid(testCaseObject.getSeriesNumber()))
                .forEach(testCaseObject -> serienTestCases.put(testCaseObject.getSeriesNumber(), testCaseObject));
        return serienTestCases.values().stream().map(this::getTestCaseReady);
    }

    public DynamicContainer getTestCaseReady(TestCaseObject testCaseObject) {
        if (isValid(testCaseObject.getSeriesNumber())) {
            testCaseObject.setName(testCaseObject.getName() + " - SN - " + testCaseObject.getSeriesNumber());
        }
        return DynamicContainer.dynamicContainer(testCaseObject.prepareAndGetDisplayName(), testCaseObject.getTestSteps());
    }

    /**
     * closing test run, close drivers and generate reports
     */
    @AfterAll
    public static void afterTests() {
        info("Ending Test Run...");
        //finish afterTest for last test case
        TestStepMonitor.afterAllSteps();
        //generate allure html report on server
        //HTML Report generation by remote parallel exec. must be handled after all threads done
        ReportBuilder reportBuilder = new ReportBuilder();
        if (PropertyResolver.isAllureReportServiceEnabled()) {
            TestRunManager.generateReportOnService();
        }
        //generate allure html report locally
        reportBuilder.generateAllureHTMLReport();
        DriverManager.cleanUp();
        reportBuilder.generateMavenTestXMLReport();
        info("Test Run is finished.");
    }

    public ApollonConfiguration getApollonConfiguration() {
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
     * override to relocate the property file
     *
     * @return path of property file
     */
    protected String getTestRunPropertiesPath() {
        return "";
    }

    /**
     * override to set up selenide
     */
    protected void setUpSelenide() {
        Configuration.reportsFolder = StringUtils.chop(PropertyResolver.getTestCaseReportLocation());
        Configuration.screenshots = false;
        Configuration.timeout = PropertyResolver.getDriverWaitTimeout();
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

    /**
     * Set up Framework using FrameworkConfiguration
     */
    protected void setUpFramework() {
        getApollonConfiguration().setTestcaseFileExtension(".tas");
    }

    private void cleanResultsByPresentOnServer() {
        if (PropertyResolver.isCleanUpResults() && PropertyResolver.isAllureReportServiceEnabled()) {
            cleanResultsByPresent();
        }
    }
}