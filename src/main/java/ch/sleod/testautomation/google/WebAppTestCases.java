package ch.sleod.testautomation.google;

import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.annotations.AfterTest;
import ch.sleod.testautomation.framework.core.annotations.BeforeTest;
import ch.sleod.testautomation.framework.core.component.PerformableTestCases;
import ch.sleod.testautomation.framework.core.runner.JUnitReportingRunner;
import com.codeborne.selenide.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

@RunWith(JUnitReportingRunner.class)
public class WebAppTestCases extends PerformableTestCases {

    /**
     * to something before single test run, e.g. reset some system properties or tools
     */
    @BeforeTest
    public static void beforeTest() {
        //to something before single test run
    }

    /**
     * do something after single test run, e.g. cleanUp driver cookies if necessary
     */
    @AfterTest
    public static void afterTest() {
//        DriverManager.getWebDriver().manage().deleteAllCookies();
    }

    /**
     * Define the relative path from project's resources root directory to the directory
     * where the test case files should be searched for inside.
     * <p>
     * Default is under resources in directory "testCases"
     *
     * @return the relative base path,
     * is not allowed to start with "/" and not to end with "/".
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
        return asList("+demo2");
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
        return "properties/DefaultTestRunProperties.properties";
    }

    @Override
    protected String getTestCaseFileFormat() {
        return ".json";
    }

    /**
     * override to set up selenide
     */
    @Override
    protected void setUpSelenide() {
//        Configuration.reportsFolder = "test-result/reports";
        Configuration.reportsFolder = StringUtils.chop(PropertyResolver.getDefaultTestCaseReportLocation());
        Configuration.screenshots = false;
    }
}
