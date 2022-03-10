package ch.qa.testautomation.framework.core.json.deserialization;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.ObjectMapperSingleton;
import ch.qa.testautomation.framework.core.json.container.*;
import ch.qa.testautomation.framework.exception.JsonProcessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.error;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static ch.qa.testautomation.framework.core.json.ObjectMapperSingleton.getObjectMapper;
import static java.util.Arrays.asList;

public class JSONContainerFactory {

    public static final String MAIN_BUILD_URL_KEY = "build.main.url";
    public static final String BUILD_ORDER_KEY = "buildOrder";
    public static final String HUB_URL_KEY = "hubURL";
    public static final String BUILD_NAME_KEY = "buildName";
    public static final String REPORT_URL = "reportUrl";
    public static final String REPORT_NAME = "reportName";
    private static int currentOrder;

    private static final ObjectMapper mapper = new ObjectMapper();

    private JSONContainerFactory() {
    }

    /**
     * checkFields the test case object via test case file
     *
     * @param jsonFilePath file path
     * @return JSONTestCase
     * @throws IOException io exception
     */
    public static JSONTestCase buildJSONTestCaseObject(String jsonFilePath) throws IOException {

        var path = FileLocator.findResource(jsonFilePath).toString();
        var jsonString = FileOperation.readFileToLinedString(path);
        var objectMapper = getObjectMapper();
        var jsonTestCase = objectMapper.readValue(jsonString, JSONTestCase.class);
        var testCaseFile = new File(path);

        var packageName = "Default";
        String[] token = testCaseFile.getParentFile().getAbsolutePath().replace("\\", "/")
                .split(PropertyResolver.getDefaultTestCaseLocation());
        if (token.length == 2) {
            packageName = token[1].replace("/", ".");
        }
        jsonTestCase.setPackage(packageName);
        jsonTestCase.setFileName(testCaseFile.getName());
        return jsonTestCase;
    }

    /**
     * Basic Method to traverse JsonNode
     *
     * @param root root node
     */
    public static void traverse(JsonNode root) {

        if (root.isObject()) {
            Iterator<String> fieldNames = root.fieldNames();

            while (fieldNames.hasNext()) {

                String fieldName = fieldNames.next();
                JsonNode fieldValue = root.get(fieldName);
                traverse(fieldValue);
            }
        } else if (root.isArray()) {

            var arrayNode = (ArrayNode) root;
            for (var i = 0; i < arrayNode.size(); i++) {
                JsonNode arrayElement = arrayNode.get(i);
                traverse(arrayElement);
            }
        } else {
            // JsonNode root represents a single value field - do something with it.

        }
    }

    /**
     * build web page configuration of web element selectors with json file
     *
     * @param jsonFilePath file path
     * @return JSONPageConfig
     * @throws IOException io exception
     */
    public static JSONPageConfig buildTestObjectConfig(String jsonFilePath) throws IOException {
        var jsonString = FileOperation.readFileToLinedString(FileLocator.findResource(jsonFilePath).toString());
        return getObjectMapper().readValue(jsonString, JSONPageConfig.class);
    }

    /**
     * build web page configuration of web element selectors with json file
     *
     * @param jsonFilePath file path
     * @return JSONTestResult
     * @throws IOException io exception
     */
    public static JSONTestResult buildJSONTestResult(String jsonFilePath) throws IOException {
        var jsonString = FileOperation.readFileToLinedString(jsonFilePath);
        return ObjectMapperSingleton.getObjectMapper().readValue(jsonString, JSONTestResult.class);
    }

    /**
     * fetch driver configuration in json file
     *
     * @param jsonFileName json file path
     * @return JSONDriverConfig
     * @throws IOException io exception
     */
    public static JSONDriverConfig getDriverConfig(String jsonFileName) throws IOException {
        var jsonString = FileOperation.readFileToLinedString(FileLocator.findResource(jsonFileName).toString());
        return getObjectMapper().readValue(jsonString, JSONDriverConfig.class);
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

                var objectMapper = getObjectMapper();
                var jsonString = FileOperation.readFileToLinedString(file.getPath());
                var jsonNode = objectMapper.readTree(jsonString);

                if (jsonNode.has(HUB_URL_KEY)) {
                    JSONDriverConfig driverConfig = objectMapper.readValue(jsonString, JSONDriverConfig.class);
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

        var jsonString = FileOperation.readFileToLinedString(FileLocator.findResource(jsonFileName).toString());
        return getObjectMapper().readValue(jsonString, JSONRunnerConfig.class);
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
    public static List<String> getHistoryFiles(int order) {
        String dirPath = PropertyResolver.getAllureReportDir() + "run" + order + "/history";
        if (new File(dirPath).exists()) {
            return FileLocator.findPaths(new File(dirPath).toPath(), Collections.singletonList("*.json"), Collections.singletonList(""), dirPath);
        } else {
            warn("No History File of Allure Report found!");
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
        var dir = new File(resultsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        results.forEach(result -> {
            try {
                var serialized = getObjectMapper().writeValueAsString(result);
                FileOperation.writeBytesToFile(serialized.getBytes(), new File(resultsDir + result.getUuid() + "-result.json"));
            } catch (IOException ex) {
                error(ex);
            }
        });
    }

    /**
     * Generate executor json and set current order of run
     */
    public static void generateExecutorJSON() {

        var objectMapper = getObjectMapper();
        var executor = objectMapper.createObjectNode();

        String url = System.getProperty(MAIN_BUILD_URL_KEY, "http://localhost:63342/framework/target/allure-report/");
        var buildOrder = 1;
        String content = getExecutorContent();
        JsonNode existingExecutor = null;
        if (!content.isEmpty()) {
            try {
                existingExecutor = objectMapper.readTree(content);
            } catch (JsonProcessingException e) {
                throw new JsonProcessException(e);
            }
        }
        if (existingExecutor != null) {
            buildOrder = existingExecutor.get(BUILD_ORDER_KEY).asInt() + 1;
        }
        String buildName = System.getProperty(BUILD_NAME_KEY, "Automated_Test_Run") + "/#" + buildOrder;
        executor.put("name", PropertyResolver.getSystemUser())
                .put("type", "junit")
                .put("url", url)
                .put(BUILD_ORDER_KEY, buildOrder)
                .put(BUILD_NAME_KEY, buildName)
                .put(REPORT_URL, url + "run" + buildOrder)
                .put(REPORT_NAME, "Allure Report of Test Run" + buildOrder);
        try {
            FileOperation.writeBytesToFile(executor.toString().getBytes(),
                    new File(PropertyResolver.getAllureResultsDir() + "executor.json"));
        } catch (IOException ex) {
            error(ex);
        }
        currentOrder = buildOrder;
    }

    public static int getCurrentOrder() {
        return currentOrder;
    }


    public static JsonNode getAllureResultObject(Path path) {
        var content = FileOperation.readFileToLinedString(path.toString());

        JsonNode node;
        try {

            node = getObjectMapper().readTree(content);
        } catch (JsonProcessingException e) {

            throw new JsonProcessException(e);
        }

        return node;
    }

    /**
     * Find all existing allure results in default allure results location
     *
     * @return list of result file path
     */
    public static List<String> getAllureResults() {
        String dirPath = PropertyResolver.getAllureResultsDir();
        return FileLocator.findPaths(Paths.get(dirPath), Collections.singletonList("*-result.json"), Collections.singletonList(""), dirPath);
    }

    /**
     * Find all existing allure results in default allure results location
     *
     * @return list of result file path
     */
    public static List<Path> listCurrentAllureResults() {
        String dirPath = PropertyResolver.getAllureResultsDir();
        return FileLocator.listRegularFilesRecursiveMatchedToName(dirPath, 1, "-result.json");
    }

    /**
     * Find all existing allure results attachments in default allure results location
     *
     * @return list of result file path
     */
    public static List<String> getAllureResultsAttachments() {
        String dirPath = PropertyResolver.getAllureResultsDir();
        return FileLocator.findPaths(Paths.get(dirPath), asList("*-attachment.*", "**/*-attachment.*"), Collections.singletonList(""), dirPath);
    }

    public static List<String> getAllureResults4Upload(String resultsDir) {
        return FileLocator.findPaths(Paths.get(resultsDir), asList("*-result.json", "*-attachment.*", "environment.properties"), Collections.singletonList(""), resultsDir);
    }

    /**
     * Get environment.properties File
     *
     * @return list of result file path
     */
    public static List<String> getEnvironmentPropertiesFile() {
        String dirPath = PropertyResolver.getAllureResultsDir();
        return FileLocator.findPaths(Paths.get(dirPath), Collections.singletonList("environment.properties"), Collections.singletonList(""), dirPath);
    }

    /**
     * Clean existing result json files
     */
    public static void cleanUpAllureResults() {
        List<String> resultFiles = getAllureResults();
        FileOperation.deleteFiles(resultFiles);
    }

    public static void archiveResults(int order) {
        String resultsDir = PropertyResolver.getAllureResultsDir();
        var targetDir = new File(resultsDir + "run" + order);
        targetDir.mkdir();
        listCurrentAllureResults().forEach(filePath -> FileOperation.moveFileTo(filePath, Paths.get(targetDir.getAbsolutePath() + "/" + filePath.getFileName())));
    }

    /**
     * Get History json Content
     *
     * @return String of history content
     */
    public static String getHistoryContent() {
        String filePath = PropertyResolver.getAllureResultsDir() + "history/history.json";
        return getJSONFileContent(filePath);
    }

    /**
     * Get Executor json Content
     *
     * @return String Executor json Content
     */
    public static String getExecutorContent() {
        String filePath = PropertyResolver.getAllureResultsDir() + "executor.json";
        return getJSONFileContent(filePath);
    }

    /**
     * @param filePath file path of config file
     * @return json object of config file
     */
    public static JsonNode getConfig(String filePath) {

        var path = FileLocator.findResource(filePath).toString();
        var content = FileOperation.readFileToLinedString(path);
        JsonNode fileData;
        try {
            fileData = mapper.readTree(content);
        } catch (JsonProcessingException e) {
            throw new JsonProcessException(e);
        }
        return fileData;
    }

    /**
     * General method of get json file content
     *
     * @param filePath file path of json file
     * @return json object of file
     */
    public static String getJSONFileContent(String filePath) {
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
            return Long.valueOf(result1.getStart() - result2.getStart()).intValue();
        }
    }
}