package example.httpclient;

import ch.qa.testautomation.framework.common.IOUtils.FileLocator;
import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class AllureReportService {

    private final Path configPath;

    public AllureReportService() {
        configPath = FileLocator.findExactFile("D:/IdeaProjects/RCH_Framework_Java-Copy", 5, new File(PropertyResolver.getReportServiceRunnerConfigFile()).getName());
    }

    public void uploadResults() throws IOException {
        String content = FileOperation.readFileToLinedString(configPath.toString());
        Map<String, String> config = parseContentToConfig(content);
        Map<String, String> params = new HashMap<>();
        params.put("project_id", config.get("project_id"));
        params.put("force_project_creation", config.get("force_project_creation"));
        String response = SimpleRestClient.sendRequest(config, params, "POST", "/send-results", getPayload(), SimpleRestClient.CONTENT_TYPE);
        SystemLogger.info("Rest Response" + "\n" + response);
    }

    private String getPayload() {
        return "";
    }

    public void generateReport() throws IOException {
        String content = FileOperation.readFileToLinedString(configPath.toString());
        Map<String, String> config = parseContentToConfig(content);
        Map<String, String> params = new HashMap<>();
        params.put("project_id", config.get("project_id"));
        params.put("execution_name", (String) config.get("execution_name"));
        params.put("execution_from", (String) config.get("execution_from"));
        params.put("execution_type", (String) config.get("execution_type"));
        String response = SimpleRestClient.sendRequest(config, params, "GET", "/generate-report", "", SimpleRestClient.CONTENT_TYPE);
        SystemLogger.info("Rest Response" + "\n" + response);
    }

    public void cleanUp() throws IOException {
        String content = FileOperation.readFileToLinedString(configPath.toString());
        Map<String, String> config = parseContentToConfig(content);
        Map<String, String> params = new HashMap<>();
        String response = SimpleRestClient.sendRequest(config, params, "GET", "/clean-results", "", SimpleRestClient.CONTENT_TYPE);
        SystemLogger.info("Rest Response" + "\n" + response);
    }

    private Map<String, String> parseContentToConfig(String content) {
        Map<String, String> config = new LinkedHashMap<>();
        Pattern pattern = Pattern.compile("[\"](\\w+)[\"]:[\\s]?[\"](.*)[\"]");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            config.put(matcher.group(1), matcher.group(2));
        }
        return config;
    }

}
