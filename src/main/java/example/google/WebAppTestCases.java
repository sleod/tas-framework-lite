package example.google;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.annotations.AfterTest;
import ch.qa.testautomation.framework.core.annotations.BeforeTest;
import ch.qa.testautomation.framework.core.component.DriverManager;
import ch.qa.testautomation.framework.core.component.PerformableTestCases;
import ch.qa.testautomation.framework.core.component.TestRunManager;
import ch.qa.testautomation.framework.core.runner.JUnitReportingRunner;
import com.codeborne.selenide.Configuration;
import org.apache.commons.lang.StringUtils;
import org.junit.runner.RunWith;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

@RunWith(JUnitReportingRunner.class)
public class WebAppTestCases extends PerformableTestCases {

    /**
     * Define actions before single test start
     * for example: reset driver with customized options
     */
    @BeforeTest
    public static void beforeTest() {
//        ChromeOptions options = new ChromeOptions();
//        options.addArguments("headless");
//        DriverManager.resetChromeDriver(options);
    }

    /**
     * Define actions before single test start
     * for example: close driver and reset driver
     */
    @AfterTest
    public static void afterTest() {
//        DriverManager.closeDriver();
//        DriverManager.setupWebDriver();
//        TestRunManager.addExtraAttachment4TestCase("C:/output/test.log");
    }

    /**
     * Define the relative path from project's resources root directory to the directory
     * where the test case files should be searched for inside.
     * <p>
     * Default is under resources in directory "testCases"
     *
     * @return the relative base path,
     * is not allowed to start with "/" and not to end with "/"..
     */
    protected String getFileBaseDir() {
        return "testCases";
    }

    /**
     * Override to define which patterns of test cases to run.
     * <p>
     * Default includes all test case files in base dir and all sub directories.
     *
     * @return the patterns of test case files to include
     */
    @Override
    protected List<String> includeFilePatterns() {
        return asList("*" + getTestCaseFileFormat(), "**/*" + getTestCaseFileFormat());
    }

    /**
     * Override to filter out some test cases not to run.
     * <p>
     * Default does not filter any test cases.
     *
     * @return the patterns of test case files to exclude
     */
    @Override
    protected List<String> excludeFilePatterns() {
        return Collections.emptyList();
    }

    /**
     * Override to filter out some test cases not to run via tag in test case
     * <p>
     * Default does not filter any test cases.
     *
     * @return the patterns of test case files to exclude
     */
    @Override
    protected List<String> getMetaFilters() {
        return asList(
                "+demo1.1"
//                ,
//                "+demo1.3"
        );
    }

    /**
     * Override to relocate the properties file for the test run
     * <p>
     * Default is "properties/TestRunProperties.properties"
     *
     * @return path to properties
     */
    @Override
    protected String getTestRunPropertiesPath() {
        return "properties/TestRunProperties.properties";
    }

    /**
     * Define the test case file format
     * Can be overridden by subclasses
     *
     * @return file format suffix
     */
    @Override
    protected String getTestCaseFileFormat() {
        return ".tas";
    }

    /**
     * override to set up filter for CSV test data selection
     *
     * @return map of filter : <column, value>
     */
    protected Map<String, String> getCSVTestDataSelectionFilter() {
        Map<String, String> filter = new LinkedHashMap<>();
        filter.putIfAbsent("testCaseId", "681736");
        return filter;
    }

    /**
     * override to set up filter for CSV test data exclusion
     *
     * @return map of filter : <column, value>
     */
    protected Map<String, String> getCSVTestDataExclusionFilter() {
        Map<String, String> filter = new LinkedHashMap<>();
//        filter.putIfAbsent("testCaseId", "681736");
        return filter;
    }

    @Override
    protected void setUpSelenide() {
        Configuration.reportsFolder = StringUtils.chop(PropertyResolver.getDefaultTestCaseReportLocation());
        Configuration.screenshots = false;
        Configuration.timeout = PropertyResolver.getSelenideTimeout();
        Configuration.savePageSource = false;
    }
}
