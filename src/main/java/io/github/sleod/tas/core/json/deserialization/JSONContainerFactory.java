package io.github.sleod.tas.core.json.deserialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.github.sleod.tas.common.IOUtils.FileLocator;
import io.github.sleod.tas.common.IOUtils.FileOperation;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.core.json.container.*;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static io.github.sleod.tas.common.logging.SystemLogger.debug;
import static io.github.sleod.tas.common.logging.SystemLogger.info;
import static io.github.sleod.tas.core.json.ObjectMapperSingleton.mapper;

public class JSONContainerFactory {

    public static final String MAIN_BUILD_URL_KEY = "build.main.url";
    public static final String BUILD_ORDER_KEY = "buildOrder";
    public static final String HUB_URL_KEY = "hubURL";
    public static final String BUILD_NAME_KEY = "buildName";
    public static final String REPORT_URL = "reportUrl";
    public static final String REPORT_NAME = "reportName";


    /**
     * checkFields the test case object via test case file
     *
     * @param jsonFilePath file path
     * @return JSONTestCase
     */
    public static JSONTestCase buildJSONTestCaseObject(String jsonFilePath) {
        String path = FileLocator.findResource(jsonFilePath).toString();
        String jsonString = FileOperation.readFileToLinedString(path);
        return buildJSONObject(jsonString, JSONTestCase.class);
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
            debug(root.toString());
        }
    }

    /**
     * build web page configuration of web element selectors with json file
     *
     * @param jsonFilePath file path
     * @return JSONPageConfig
     */
    public static JSONPageConfig readTestObjectConfig(String jsonFilePath) {
        String jsonString = FileOperation.readFileToLinedString(FileLocator.findResource(jsonFilePath).toString());
        return buildJSONObject(jsonString, JSONPageConfig.class);
    }

    /**
     * build web page configuration of web element selectors with json file
     *
     * @param jsonFilePath file path
     * @return JSONTestResult
     */
    public static JSONTestResult readJSONTestResult(String jsonFilePath) {
        String jsonString = FileOperation.readFileToLinedString(jsonFilePath);
        return buildJSONObject(jsonString, JSONTestResult.class);
    }

    /**
     * fetch driver configuration in json file
     *
     * @param folderPath json config file folder path
     * @return list of driver configs or preset driver config single
     */
    public static Map<String, JSONDriverConfig> getDriverConfigs(String folderPath, String configFileName) {
        info("Try to find driver config in: " + folderPath);
        File[] files = FileOperation.getResourceFolderFiles(folderPath);
        Map<String, JSONDriverConfig> configs = new LinkedHashMap<>();
        for (File file : files) {
            if (file.getName().endsWith(".json")) {
                String jsonString = FileOperation.readFileToLinedString(file.getPath());
                if (jsonString.contains(HUB_URL_KEY)) {
                    JSONDriverConfig driverConfig = buildJSONObject(jsonString, JSONDriverConfig.class);
                    if (file.getName().equalsIgnoreCase(configFileName)) {
                        info("Load given Driver Config: " + configFileName);
                        return Collections.singletonMap(configFileName, driverConfig);
                    } else {
                        configs.put(file.getName(), driverConfig);
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
     */
    public static JSONRunnerConfig getRunnerConfig(String jsonFileName) {
        String jsonString = FileOperation.readFileToLinedString(FileLocator.findResource(jsonFileName).toString());
        return buildJSONObject(jsonString, JSONRunnerConfig.class);
    }

    /**
     * load existing allure-results order by startNow time
     *
     * @return list of json test result objects
     */
    public static List<JSONTestResult> loadJSONAllureTestResults() {
        return sortListWithStartTime((LinkedList<JSONTestResult>) getExistingAllureTestResults());
    }

    /**
     * load existing allure-results order by startNow time
     *
     * @return list of json test result objects
     */
    public static List<JSONTestResult> getExistingAllureTestResults() {
        List<String> resultFiles = getAllureResults();
        LinkedList<JSONTestResult> results = new LinkedList<>();
        for (String fpath : resultFiles) {
            results.add(readJSONTestResult(fpath));
        }
        return results;
    }

    /**
     * fetch history files for tends static
     *
     * @return list of history files
     */
    public static List<String> getHistoryFiles(int order) {
        String dirPath = PropertyResolver.getAllureReportDirectory() + "run" + order + "/history";
        if (new File(dirPath).exists()) {
            return FileLocator.findPaths(new File(dirPath).toPath(), Collections.singletonList("*.json"), Collections.singletonList(""), dirPath);
        } else {
            info("No History File of Allure Report found!");
            return Collections.emptyList();
        }
    }

    /**
     * regenerate allure results with given json test results
     *
     * @param results list of json test result conform to allure result schema
     */
    public static List<String> regenerateAllureResults(List<JSONTestResult> results) {
        String resultsDir = PropertyResolver.getAllureResultsDirectory();
        File dir = new File(resultsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        List<String> resultFilePaths = new ArrayList<>(results.size());
        results.forEach(result -> {
            String path = resultsDir + result.getUuid() + "-result.json";
            resultFilePaths.add(path);
            try {
                String serialized = mapper().writeValueAsString(result);
                FileOperation.writeStringToFile(serialized, path);
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_WRITING, ex, path);
            }
        });
        return resultFilePaths;
    }

    public static JsonNode getAllureResultObject(Path path) {
        String content = FileOperation.readFileToLinedString(path.toString());
        JsonNode node;
        try {
            node = mapper().readTree(content);
        } catch (JsonProcessingException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, path);
        }
        return node;
    }

    /**
     * Find all existing allure results in default allure results location
     *
     * @return list of result file path
     */
    public static List<String> getAllureResults() {
        File dir = new File(PropertyResolver.getAllureResultsDirectory());
        return FileLocator.findPaths(dir.toPath(), Collections.singletonList("*-result.json"), Collections.singletonList(""), dir.getPath());
    }

//    /**
//     * Find all existing allure results attachments in default allure results location
//     *
//     * @return list of result file path
//     */
//    public static List<String> getAllureResultsAttachments(String resultsDir) {
//        File dir = new File(resultsDir);
//        return FileLocator.findPaths(dir.toPath(), List.of("*-attachment.*", "**/*-attachment.*"), Collections.singletonList(""), dir.getPath());
//    }
//
//    /**
//     * Find all existing allure results attachments in default allure results location
//     *
//     * @return list of result file path
//     */
//    public static List<String> getAllureResults4Upload(String resultsDir) {
//        File dir = new File(resultsDir);
//        return FileLocator.findPaths(dir.toPath(), Collections.singletonList("*-result.json"), Collections.singletonList(""), dir.getPath());
//    }

    /**
     * Find all existing allure results attachments in default allure results location
     *
     * @return list of result file path
     */
    public static List<String> getAllureExtra4Upload() {
        File dir = new File(PropertyResolver.getAllureResultsDirectory());
        return FileLocator.findPaths(dir.toPath(), List.of("*.properties", "*.json"), Collections.singletonList("*-result.json"), dir.getPath());
    }

    /**
     * Clean existing result json files
     */
    public static void cleanUpAllureResults() {
        List<String> resultFiles = getAllureResults();
        FileOperation.deleteFiles(resultFiles);
    }

    public static String archiveResults(int order) {
        String resultsDir = PropertyResolver.getAllureResultsDirectory() + "run" + order;
        File targetDir = new File(resultsDir);
        targetDir.mkdir();
        getAllureResults().stream().map(File::new)
                .forEach(file -> FileOperation.moveFileTo(file.toPath(), Paths.get(targetDir.getAbsolutePath() + "/" + file.getName())));
        return resultsDir;
    }

    /**
     * Get History json Content
     *
     * @return String of history content
     */
    public static String getHistoryContent() {
        String filePath = PropertyResolver.getAllureResultsDirectory() + "history/history.json";
        return getJSONFileContent(filePath);
    }

    /**
     * Get Executor json Content
     *
     * @return String Executor json Content
     */
    public static String getExecutorContent() {
        String filePath = PropertyResolver.getAllureResultsDirectory() + "executor.json";
        return getJSONFileContent(filePath);
    }

    /**
     * @param filePath file path of config file
     * @return json object of config file
     */
    public static JsonNode getConfig(String filePath) {
        String path = FileLocator.findResource(filePath).toString();
        String content = FileOperation.readFileToLinedString(path);
        JsonNode fileData;
        try {
            fileData = mapper().readTree(content);
        } catch (JsonProcessingException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, filePath);
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
        if (FileOperation.isFileExists(filePath)) {
            return FileOperation.readFileToLinedString(filePath);
        } else {
            return "";
        }
    }

    /**
     * General method of get json file content
     *
     * @param inputStream inputStream
     * @return json object of file
     */
    public static String getJSONFileContent(InputStream inputStream) {
        return FileOperation.readFileToLinedString(inputStream);
    }

    public static <T> T buildJSONObject(String jsonString, Class<T> myClass) {
        try {
            return mapper().readValue(jsonString, myClass);
        } catch (JsonProcessingException ex) {
            throw new ExceptionBase(ExceptionErrorKeys.EXCEPTION_BY_DESERIALIZATION, ex, myClass.getSimpleName());
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