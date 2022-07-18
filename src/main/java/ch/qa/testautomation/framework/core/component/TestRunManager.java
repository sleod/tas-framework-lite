package ch.qa.testautomation.framework.core.component;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.enumerations.TestType;
import ch.qa.testautomation.framework.common.logging.ScreenCapture;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.container.JSONRunnerConfig;
import ch.qa.testautomation.framework.core.json.container.JSONTestCase;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.core.report.ReportBuilder;
import ch.qa.testautomation.framework.core.report.allure.ReportBuilderAllureService;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.*;
import static ch.qa.testautomation.framework.common.utils.ObjectWriterReader.WriteObject;
import static ch.qa.testautomation.framework.common.utils.ObjectWriterReader.readObject;

public class TestRunManager {

    private static PerformableTestCases performer;
    private static String currentISOAppName;
    private static String currentAppName;
    private static String currentAppActivity;
    private static JSONRunnerConfig runnerConfig = null;

    public static PerformableTestCases getPerformer() {
        return performer;
    }

    public static void setPerformer(PerformableTestCases performer) {
        TestRunManager.performer = performer;
    }

    public static String getCurrentAppName() {
        return currentAppName;
    }

    public static String getCurrentAppActivity() {
        return currentAppActivity;
    }

    public static String getCurrentISOAppName() {
        return currentISOAppName;
    }

    /**
     * allow add extra plain text attachment for single test case
     *
     * @param filePath file path
     */
    public static void addExtraAttachment4TestCase(String filePath) {
        List<String> extensions = Arrays.asList("txt", "csv", "log");
        if (extensions.contains(FileOperation.getFileNameExtension(filePath))) {
            File attachment = new File(filePath);
            if (attachment.exists()) {
                ReportBuilder.addExtraAttachment4TestCase(attachment);
            } else {
                warn("The file extension is not allowed for attachment! Only allowed extensions: "
                        + extensions.stream().map(String::valueOf).collect(Collectors.joining(",")));
            }
        } else {
            warn("Extra Attachment File does not exist! ->" + filePath);
        }

    }

    /**
     * filter test case with meta tags
     *
     * @param metaFilters  defined meta tags
     * @param jsonTestCase test case
     * @return if to be added
     */
    public static boolean filterTestCase(List<String> metaFilters, JSONTestCase jsonTestCase) {
        if (metaFilters == null || metaFilters.isEmpty()) {
            return true;
        }
        metaFilters.replaceAll(metaFilter -> metaFilter.replace("@", ""));
        List<String> testCaseMetas = jsonTestCase.getMeta();
        boolean toBeAdd = false;
        for (String metaTag : testCaseMetas) {
            String exclude = metaTag.replace("@", "-");
            String include = metaTag.replace("@", "+");
            if (metaFilters.contains(exclude)) {
                toBeAdd = false;
                break;
            }
            if (metaFilters.contains(include)) {
                toBeAdd = true;
            }
        }
        return toBeAdd;
    }

    /**
     * find files of test cases
     *
     * @param fileBaseDir         file directory
     * @param includeFilePatterns include patterns
     * @param excludeFilePatterns exclude patterns
     * @return file paths of test cases
     */
    public static List<String> filePaths(String fileBaseDir, List<String> includeFilePatterns, List<String> excludeFilePatterns) {
        List<String> paths = FileLocator.findPaths(FileLocator.findResource(fileBaseDir),
                includeFilePatterns,
                excludeFilePatterns,
                fileBaseDir);
        log("TRACE", "Paths found: " + Arrays.toString(paths.toArray()));
        return paths;
    }

    /**
     * init test cases with file paths of json filtered with meta notation
     *
     * @param filePaths   file paths
     * @param metaFilters meta filters
     */
    public static void initTestCases(List<String> filePaths, List<String> metaFilters) throws IOException {
        List<String> selectedIds = Collections.emptyList();
        if (filePaths.isEmpty()) {
            throw new RuntimeException("There is no json test case in defined folder! ->" + PropertyResolver.getDefaultTestCaseLocation());
        }
        if (!metaFilters.isEmpty()) {
            trace("Filters: " + Arrays.toString(metaFilters.toArray()));
        } else {
            warn("No Filter is set, all found test case will be executed!");
        }
        if (metaFilters.contains("")) {
            warn("meta filters contains empty value!");
        }
        List<TestCaseObject> testCaseObjects = new LinkedList<>();
        for (String filePath : filePaths) {
            JSONTestCase jsonTestCase = JSONContainerFactory.buildJSONTestCaseObject(filePath);
            //filter with meta tags
            if (filterTestCase(metaFilters, jsonTestCase)) {
                List<TestCaseObject> normalizedTestCases = normalizeRepeatTestCases(new TestCaseObject(jsonTestCase));
                for (TestCaseObject n_testCaseObject : normalizedTestCases) {
                    if (isSelected(n_testCaseObject.getTestCaseId(), selectedIds))
                        testCaseObjects.add(n_testCaseObject);
                }
            }
        }
        getPerformer().setTestCaseObjects(testCaseObjects);
    }

    public static void cleanResultsByPresent() {
        if (PropertyResolver.isAllureReportService()) {
            new ReportBuilderAllureService().cleanResultsByPresent();
        }
    }

    public static void uploadSingleTestRunReport() {
        if (PropertyResolver.isAllureReportService()) {
            new ReportBuilderAllureService().uploadAllureResults();
        }
    }

    public static void generateReportOnService() {
        if (PropertyResolver.isAllureReportService()) {
            new ReportBuilderAllureService().generateReportOnService();
        }
    }


    /**
     * load driver while init test case object
     *
     * @param jsonTestCase json test case
     * @param testCaseName test case name
     */
    public static void loadDriver(JSONTestCase jsonTestCase, String testCaseName) {
        trace("Load driver for: " + testCaseName);
        //load all driver config
        TestType type = TestType.valueOf(jsonTestCase.getType().toUpperCase());
        switch (type) {
            case WEB_APP:
                //init web driver for runs
                DriverManager.setupWebDriver();
                //set screenshot taker
                ScreenCapture.setScreenTaker(DriverManager.getWebDriverProvider());
                break;
            case REST:
                DriverManager.setupRestDriver();
                break;
            case MOBILE_WEB_APP:
                DriverManager.setupRemoteWebDriver();
                ScreenCapture.setScreenTaker(DriverManager.getRemoteWebDriverProvider());
                break;
            case APP:
                DriverManager.setupNonDriver();
                break;
        }
    }

    /**
     * check if given test case is selected to run and update the coverage with selection
     *
     * @param id          test case id
     * @param selectedIds list if selected id
     * @return if current test case selected
     */
    private static boolean isSelected(String id, List<String> selectedIds) {
        if (!PropertyResolver.isTFSSyncEnabled()) {
            return true;
        } else if (selectedIds == null || selectedIds.isEmpty()) {
            warn("List of Selected IDs is Empty!");
            return false;
        }
        return selectedIds.contains(id);
    }

    /**
     * Normalize test case with multiple test data, for example: test data in csv file
     *
     * @param testCaseObject test case object
     * @return list of test case objects
     */
    private static List<TestCaseObject> normalizeRepeatTestCases(TestCaseObject testCaseObject) {
        if (testCaseObject.getTestDataContainer().isRepeat()) {
            ArrayList<TestCaseObject> normCases = new ArrayList<>(testCaseObject.getTestDataContainer().getDataContentSize());
            //for each row of test data, create new test case object
            List<Map<String, Object>> dataContent = testCaseObject.getTestDataContainer().getDataContent();
            for (int index = 0; index < dataContent.size(); index++) {
                Map<String, Object> testData = dataContent.get(index);
                TestCaseObject new_tco = new TestCaseObject(testCaseObject.getTestCase(), Collections.singletonList(testData), " - Variante " + (index + 1));
                if (!isFiltered(testData)) {
                    continue;
                }
                if (PropertyResolver.isTFSSyncEnabled()
                        && !testData.containsKey("testCaseId")
                        && testData.get("testCaseId") != null
                        && !testData.get("testCaseId").toString().isEmpty()) {
                    throw new RuntimeException("To return result back to TFS the test case id is required in csv or db data. " +
                            "Please set test case id with column name 'testCaseId' for every test data line! Or set the value with '-' to avoid execution.");
                } else {
                    if (PropertyResolver.isTFSSyncEnabled()) {
                        String testCaseId = testData.get("testCaseId").toString();
                        if (!testCaseId.equalsIgnoreCase("-")) {//avoid execution with '-'
                            new_tco.setTestCaseId(testCaseId);
                            testCaseObject.getTestCase().addCoverage(testCaseId);
                            addNormCase(normCases, new_tco);
                        } else {
                            trace(new_tco.getName() + " is ignored.");
                        }
                    } else {
                        addNormCase(normCases, new_tco);
                    }
                }
            }
            return normCases;
        } else {
            ArrayList<TestCaseObject> singleList = new ArrayList<>(1);
            if (PropertyResolver.isTFSSyncEnabled()) {
                if ((testCaseObject.getTestCaseId() == null
                        || testCaseObject.getTestCaseId().isEmpty())) {
                    throw new RuntimeException("To return result back to TFS the test case id is required. " +
                            "Please set test case id with attribute 'testCaseId' for every test case file or set the value with '-' to avoid execution.");
                } else if (testCaseObject.getTestCaseId().equalsIgnoreCase("-")) {
                    warn(testCaseObject.getName() + " testCaseId is empty!");
                } else {
                    addNormCase(singleList, testCaseObject);
                }
            } else {
                addNormCase(singleList, testCaseObject);
            }
            return singleList;
        }
    }

    private static boolean isFiltered(Map<String, Object> testData) {
        Map<String, String> selection = performer.getCSVTestDataSelectionFilter();
        Map<String, String> exclusion = performer.getCSVTestDataExclusionFilter();
        if (exclusion.isEmpty() && selection.isEmpty()) {
            return true;
        } else if (!exclusion.isEmpty() && !selection.isEmpty()) {
            warn("Both selection and exclusion filters are filled with data. Both filters are ignored!");
            return true;
        } else {
            if (!selection.isEmpty()) {
                return containedInFilter(testData, selection);
            } else {
                return !containedInFilter(testData, exclusion);
            }
        }
    }

    private static boolean containedInFilter(Map<String, Object> testData, Map<String, String> filter) {
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (testData.get(key) != null && testData.get(key).equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * handling series number and build sequenced test case runner
     *
     * @param normCases list of single cases
     * @param tco       test case object
     */
    private static void addNormCase(ArrayList<TestCaseObject> normCases, TestCaseObject tco) {
        if (tco.getSeriesNumber() != null && !tco.getSeriesNumber().isEmpty()) {
            addSequencedCase(tco);
        }
        normCases.add(tco);
    }

    /**
     * Add sequenced test case into global list
     *
     * @param tco test case object
     */
    private static void addSequencedCase(TestCaseObject tco) {
        String seriesNumber = tco.getSeriesNumber();
        int index = seriesNumber.lastIndexOf(".");
        if (index > 0) {
            getPerformer().addSequenceCaseRunner(seriesNumber.substring(0, index), tco);
        } else {
            throw new RuntimeException("Series Number has wrong format!! Please Check the form of Series Number for: "
                    + seriesNumber + "\n" + "Format of SeriesNumber: <context1>.<context2>...<number>");
        }
    }



    /**
     * copy resources files in defined locations to current local folder.
     */
    public static void retrieveResources(String propertyFilePath) {
        boolean isDefaultPath = PropertyResolver.loadTestRunProperties(propertyFilePath);
        try {
            if (isDefaultPath) {
                URL folderInJar = TestRunManager.class.getClassLoader().getResource("properties");
                if (folderInJar != null && folderInJar.toURI().getScheme().equals("jar")) {
                    File jarFile = new File(folderInJar.getFile()).getParentFile();
                    String baseDir = FileLocator.getProjectBaseDir() + "/src/main/resources/";
                    String jarPath = "jar:file:" + jarFile.getAbsolutePath().split("file:")[1].replace("\\", "/") + "/";
                    trace("Jar Path: " + jarPath);
                    URI jarUri = URI.create(jarPath);
                    //retrieve property file and rename to normal
                    File propertyFile = new File(baseDir + "properties/TestRunProperties.properties");
                    FileOperation.retrieveFileFromResources("properties/DefaultTestRunProperties.properties", propertyFile);
                    //build file system and find folders
                    for (String folder : PropertyResolver.getAllPropertiesWith("location")) {
                        trace("Property listed folder: " + folder);
                        new File(baseDir + folder).mkdir();
                    }
                }
            }
        } catch (URISyntaxException ex) {
            throw new RuntimeException("Error while retrieve data from resources!\n" + ex.getMessage());
        }
    }

    /**
     * Store Test results to DB
     *
     * @param testCaseObjects list of test case objects
     */
    public static void storeResultsToDB(List<TestCaseObject> testCaseObjects) {
        //todo: store results to DB
    }

    /**
     * Restore Cookies and URL of Web Driver
     *
     * @param webDriver Web Driver
     */
    @SuppressWarnings("unchecked")
    public static void restoreSessions(WebDriver webDriver) {
        if (new File("target/logs/cookies.data").exists()) {
            List<Cookie> cookies = (ArrayList<Cookie>) readObject(new ArrayList<Cookie>(), "target/logs/cookies.data");
            String currentURL = (String) readObject("", "target/logs/currentURL.data");
            if (currentURL != null && !currentURL.isEmpty()) {
                webDriver.navigate().to(currentURL);
            }
            if (!cookies.isEmpty()) {
                cookies.forEach(cookie -> webDriver.manage().addCookie(cookie));
            } else {
                throw new RuntimeException("Failed on reload Cookie from retest Step!");
            }
            if (currentURL != null && !currentURL.isEmpty()) {
                webDriver.navigate().to(currentURL);
            }
        }
    }

    /**
     * Store Cookies and URL of Web Driver
     *
     * @param webDriver Web Driver
     */
    public static void storeSessions(WebDriver webDriver) {
        WriteObject(new ArrayList<>(webDriver.manage().getCookies()), "target/logs/cookies.data");
        WriteObject(webDriver.getCurrentUrl(), "target/logs/currentURL.data");
    }

    /**
     * store failed step for next retry
     *
     * @param testCaseFolder test case object
     * @param testCaseName   test case object
     * @param stepOrder      oder of step
     */
    public static void storeRetryStep(String testCaseFolder, String testCaseName, int stepOrder) {
        String retryTestCaseID = testCaseFolder + "." + testCaseName;
        WriteObject(retryTestCaseID, "target/logs/retryTestCaseID.data");
        WriteObject(stepOrder, "target/logs/stepOrder.data");
    }

    /**
     * load logged test step order number for retry
     *
     * @return number of step order
     */
    public static int loadRetryStepOrder() {
        if (new File("target/logs/stepOrder.data").exists()) {
            return (Integer) readObject("", "target/logs/stepOrder.data");
        } else {
            return -1;
        }
    }

    /**
     * load logged test case id for retry
     *
     * @return test case id
     */
    public static String loadRetryTestCaseID() {
        if (new File("target/logs/retryTestCaseID.data").exists()) {
            return (String) readObject("", "target/logs/retryTestCaseID.data");
        } else {
            return "";
        }
    }

    /**
     * the file named with "xxxx-test-data-global.json" will be loaded automatically
     */
    public static void loadGlobalTestData() {
        List<Path> paths = FileLocator.listRegularFilesRecursiveMatchedToName(
                FileLocator.findResource(PropertyResolver.getDefaultTestDataLocation()).toString(), 5, "testdata-global");
        if (paths.size() > 1) {
            throw new RuntimeException("Test Data File with name 'xxx-testdata-global.json' should be single in testData folder!");
        } else if (paths.size() == 1) {
            TestDataContainer.loadGlobalTestData(paths.get(0));
        } else {
            trace("No global test data file detected. Please ignore this message if no global test data is required.");
            trace("Add file with name xxx-testdata-global.json into testData/ folder necessarily");
        }
    }

    public static boolean feedbackAfterSingleTest() {
        return runnerConfig.feedbackAfterSingleTest();
    }

}
