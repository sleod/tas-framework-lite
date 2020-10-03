package ch.sleod.testautomation.framework.core.json.deserialization;

import ch.sleod.testautomation.framework.common.IOUtils.FileLocator;
import ch.sleod.testautomation.framework.common.IOUtils.FileOperation;
import ch.sleod.testautomation.framework.configuration.PropertyResolver;
import ch.sleod.testautomation.framework.core.controller.ExternAppController;
import ch.sleod.testautomation.framework.core.json.container.*;
import ch.sleod.testautomation.framework.common.logging.SystemLogger;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import static java.util.Arrays.asList;

public class JSONContainerFactory {

    /**
     * checkFields the test case object via test case file
     *
     * @param jsonFilePath file path
     * @return JSONTestCase
     * @throws IOException io exception
     */
    public static JSONTestCase buildJSONTestCaseObject(String jsonFilePath) throws IOException {
        String path = FileLocator.findResource(jsonFilePath).toString();
        String jsonString = FileOperation.readFileToLinedString(path);
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        JSONTestCase jsonTestCase = new ObjectMapper().readValue(jsonObject.toString(), JSONTestCase.class);
        File testCaseFile = new File(path);
        jsonTestCase.setPackage(testCaseFile.getParentFile().getName());
        jsonTestCase.setFileName(testCaseFile.getName());
        return jsonTestCase;
    }

    /**
     * build web page configuration of web element selectors with json file
     *
     * @param jsonFilePath file path
     * @return JSONPageConfig
     * @throws IOException io exception
     */
    public static JSONPageConfig buildTestObjectConfig(String jsonFilePath) throws IOException {
        String jsonString = FileOperation.readFileToLinedString(FileLocator.findResource(jsonFilePath).toString());
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        return new ObjectMapper().readValue(jsonObject.toString(), JSONPageConfig.class);
    }

    /**
     * build web page configuration of web element selectors with json file
     *
     * @param jsonFilePath file path
     * @return JSONTestResult
     * @throws IOException io exception
     */
    public static JSONTestResult buildJSONTestResult(String jsonFilePath) throws IOException {
        String jsonString = FileOperation.readFileToLinedString(jsonFilePath);
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        return new ObjectMapper().readValue(jsonObject.toString(), JSONTestResult.class);
    }

    /**
     * fetch driver configuration in json file
     *
     * @param jsonFileName json file path
     * @return JSONDriverConfig
     * @throws IOException io exception
     */
    public static JSONDriverConfig getDriverConfig(String jsonFileName) throws IOException {
        String jsonString = FileOperation.readFileToLinedString(FileLocator.findResource(jsonFileName).toString());
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        return new ObjectMapper().readValue(jsonObject.toString(), JSONDriverConfig.class);
    }

    /**
     * fetch driver configuration in json file
     *
     * @param folderPath json config file folder path
     * @return list of driver config
     * @throws IOException io exception
     */
    public static List<JSONDriverConfig> getDriverConfigs(String folderPath, String defaultConfigFileName) throws IOException {
        File[] files = FileOperation.getResourceFolderFiles(folderPath);
        LinkedList<JSONDriverConfig> configs = new LinkedList<>();
        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                String jsonString = FileOperation.readFileToLinedString(file.getPath());
                JSONObject jsonObject = JSONObject.fromObject(jsonString);
                if (jsonObject.containsKey("hubURL")) {
                    JSONDriverConfig driverConfig = new ObjectMapper().readValue(jsonObject.toString(), JSONDriverConfig.class);
                    if (file.getName().equalsIgnoreCase(defaultConfigFileName)) {
                        configs.addFirst(driverConfig);
                    } else {
                        configs.addLast(driverConfig);
                    }
                }
            }
        }
        return configs;
    }

    /**
     * fetch runner configuration in json file
     *
     * @param jsonFileName json file path
     * @return JSONDriverConfig
     * @throws IOException io exception
     */
    public static JSONRunnerConfig getRunnerConfig(String jsonFileName) throws IOException {
        String jsonString = FileOperation.readFileToLinedString(Objects.requireNonNull(FileLocator.findResource(jsonFileName)).toString());
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        return new ObjectMapper().readValue(jsonObject.toString(), JSONRunnerConfig.class);
    }


    /**
     * fetch runner configuration in json file
     *
     * @param jsonFileName json file path
     * @return JSONDriverConfig
     * @throws IOException io exception
     */
    public static JSONRunnerConfig loadRunnerConfig(String jsonFileName) throws IOException {
        String jsonString = FileOperation.readFileToLinedString(FileLocator.loadResource(jsonFileName));
        JSONObject jsonObject = JSONObject.fromObject(jsonString);
        return new ObjectMapper().readValue(jsonObject.toString(), JSONRunnerConfig.class);
    }

    /**
     * load existing allure-results order by startNow time
     *
     * @return list of json test result objects
     * @throws IOException io exception
     */
    public static List<JSONTestResult> loadJSONAllureTestResults() throws IOException {
        return sortListWithStartTime((LinkedList<JSONTestResult>) getExistingAllureTestResults());
    }

    /**
     * load existing allure-results order by startNow time
     *
     * @return list of json test result objects
     * @throws IOException io exception
     */
    public static List<JSONTestResult> getExistingAllureTestResults() throws IOException {
        List<String> resultFiles = getAllureResults();
        LinkedList<JSONTestResult> results = new LinkedList<>();
        for (String fpath : resultFiles) {
            results.add(buildJSONTestResult(fpath));
        }
        return results;
    }

    /**
     * fetch history files for tends static
     *
     * @return list of history files
     */
    public static List<String> getHistoryFiles() {
        String dirPath = PropertyResolver.getAllureReportDir() + "/history";
        if (new File(dirPath).exists()) {
            return FileLocator.findPaths(new File(dirPath).toPath(), Collections.singletonList("*.json"), Collections.singletonList(""), dirPath);
        } else {
            SystemLogger.warn("No History File of Allure Report found!");
            return Collections.emptyList();
        }
    }

    /**
     * regenerate allure results with given json test results
     *
     * @param results list of json test result conform to allure result schema
     */
    public static void regenerateAllureResults(List<JSONTestResult> results) {
        String resultsDir = PropertyResolver.getAllureResultsDir();
        File dir = new File(resultsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        results.forEach(result -> {
            try {
                String serialized = new ObjectMapper().writeValueAsString(result);
                FileOperation.writeBytesToFile(serialized.getBytes(), new File(resultsDir + "/" + result.getUuid() + "-result.json"));
            } catch (IOException ex) {
                SystemLogger.error(ex);
            }
        });
        generateExecutorJSON();
    }

    /**
     * Generate executor json
     */
    private static void generateExecutorJSON() {
        JSONObject executor = new JSONObject();
        String url = System.getProperty("executor.build.url", "http://localhost:63342/framework/target/allure-report/");
        int buildOrder = 1;
        String content = getExecutorContent();
        JSONObject existingExecutor = null;
        if (!content.isEmpty()) {
            existingExecutor = JSONObject.fromObject(content);
        }
        if (existingExecutor != null) {
            buildOrder = existingExecutor.getInt("buildOrder") + 1;
        }
        String name = System.getProperty("executor.name", PropertyResolver.getSystemUser());
        String buildName = System.getProperty("executor.build.name", "Automated_Test_Run") + "/#" + buildOrder;
        executor.element("name", name).element("type", "junit").element("url", url)
                .element("buildOrder", buildOrder).element("buildName", buildName)
                .element("reportUrl", url).element("reportName", "Allure Report of Test Run");
        String resultsDir = PropertyResolver.getAllureResultsDir();
        try {
            FileOperation.writeBytesToFile(executor.toString().getBytes(), new File(resultsDir + "/executor.json"));
        } catch (IOException ex) {
            SystemLogger.error(ex);
        }
    }

    /**
     * Execute system command to generate allure report using allure executable
     */
    public static void generateAllureReport() {
        String resultsPath = PropertyResolver.getAllureResultsDir();
        String reportPath = PropertyResolver.getAllureReportDir();
        ExternAppController.executeCommand("allure generate " + resultsPath + " --clean -o " + reportPath);
    }

    /**
     * Find all existing allure results in default allure results location
     *
     * @return list of result file path
     */
    public static List<String> getAllureResults() {
        String dirPath = PropertyResolver.getAllureResultsDir();
        return FileLocator.findPaths(new File(dirPath).toPath(), asList("*-result.json", "**/*-result.json"), Collections.singletonList(""), dirPath);
    }

    /**
     * Clean existing result json files
     */
    public static void cleanUpAllureResults() {
        List<String> resultFiles = getAllureResults();
        FileOperation.deleteFiles(resultFiles);
    }

    /**
     * Get History json Content
     *
     * @return String of history content
     */
    public static String getHistoryContent() {
        String filePath = PropertyResolver.getAllureResultsDir() + "/history/history.json";
        return getJSONFileContent(filePath);
    }

    /**
     * Get Executor json Content
     *
     * @return String Executor json Content
     */
    public static String getExecutorContent() {
        String filePath = PropertyResolver.getAllureResultsDir() + "/executor.json";
        return getJSONFileContent(filePath);
    }

    /**
     * @param filePath file path of config file
     * @return json object of config file
     */
    public static JSONObject getConfig(String filePath) {
        String path = Objects.requireNonNull(FileLocator.findResource(filePath)).toString();
        String content = FileOperation.readFileToLinedString(path);
        return JSONObject.fromObject(content);
    }

    /**
     * General method of get json file content
     *
     * @param filePath file path of json file
     * @return json object of file
     */
    private static String getJSONFileContent(String filePath) {
        if (Files.exists(new File(filePath).toPath())) {
            return FileOperation.readFileToLinedString(filePath);
        } else {
            return "";
        }
    }

    /**
     * sort json test result object
     *
     * @param results list of json test result object
     * @return sorted list
     */
    private static List<JSONTestResult> sortListWithStartTime(LinkedList<JSONTestResult> results) {
        results.sort(new StartTimeComparator());
        return results;
    }

    /**
     * comparator for sort list
     */
    private static class StartTimeComparator implements Comparator<JSONTestResult> {
        @Override
        public int compare(JSONTestResult result1, JSONTestResult result2) {
            return new Long(result1.getStart() - result2.getStart()).intValue();
        }
    }
}