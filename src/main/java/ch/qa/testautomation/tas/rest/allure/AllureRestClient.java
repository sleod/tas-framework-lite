package ch.qa.testautomation.tas.rest.allure;

import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import ch.qa.testautomation.tas.rest.base.SimpleRestDriver;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.tas.rest.base.RestClientBase.checkResponse;

/**
 * Provides methods to communicate with the Allure service
 */
public class AllureRestClient {

    /**
     * Error messages
     */
    public static final String RESULT_NOT_SEND_MESSAGE = "The results could not be sent. Response Code: ";

    /**
     * Error messages
     */
    public static final String REPORT_NOT_GENERATED_MESSAGE = "The report could not be generated. Response Code: ";
    /**
     * Error messages
     */
    public static final String HISTORY_NOT_DELETED_MESSAGE = "The history could not be deleted. Response Code: ";
    /**
     * Error messages
     */
    public static final String RESULT_NOT_DELETED_MESSAGE = "The results could not be deleted. Response Code: ";
    /**
     * Parameter name
     */
    public static final String PROJECT_ID_PARAM = "project_id";
    /**
     * Error message when ProjectId is not defined
     */
    public static final String PROJECT_ID_IS_NOT_DEFINED = "The parameter " + PROJECT_ID_PARAM + " in the file reportServiceRunnerConfig.json must not be empty";
    /**
     * Parameter name
     */
    public static final String EXECUTION_NAME_PARAM = "execution_name";
    /**
     * Parameter name
     */
    public static final String EXECUTION_FROM_PARAM = "execution_from";
    /**
     * Parameter name
     */
    public static final String EXECUTION_TYPE_PARAM = "execution_type";
    /**
     * Parameter name
     */
    public static final String FORCE_PROJECT_CREATION_PARAM = "force_project_creation";
    /**
     * Builder for the transfer container
     */
    private final TransferFileBuilder transferFileBuilder;
    /**
     * Configuration parameters for the Allure service
     */
    private final Map<String, String> allureServiceConfig;
    /**
     * Rest driver
     */
    private final SimpleRestDriver restDriver;

    /**
     * Constructor
     *
     * @param config Map with the configuration parameters
     */
    public AllureRestClient(Map<String, String> config) {
        restDriver = new SimpleRestDriver();
        if (config.get(PROJECT_ID_PARAM).isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, PROJECT_ID_IS_NOT_DEFINED);
        }
        this.allureServiceConfig = config;
        this.transferFileBuilder = new TransferFileBuilder();
    }

    /**
     * Uploads the content of the result JSON and attachment files to the Allure service
     *
     * @param file4Upload Map with the files to be uploaded (Key=FileName, Value=Base64Content)
     */
    public void uploadAllureResultFiles(Map<String, String> file4Upload) {
        ObjectNode transferContainer = transferFileBuilder.prepareFileTransferNode(file4Upload);
        Response response = sendResults(transferContainer);
        checkResponse(response, RESULT_NOT_SEND_MESSAGE);
    }

    /**
     * Uploads extra files to the Allure service
     */
    public void uploadExtraFiles() {
        List<String> resultsPaths = JSONContainerFactory.getAllureExtra4Upload();
        ObjectNode transferContainer = transferFileBuilder.prepareFileTransferContainer(resultsPaths);
        debug("Find allure extra files for upload: " + resultsPaths.stream().collect(Collectors.joining(System.lineSeparator())));
        Response response = sendResults(transferContainer);
        checkResponse(response, RESULT_NOT_SEND_MESSAGE);
    }

    /**
     * Triggers the command to generate the report
     */
    public void generateReportOnService() {
        Map<String, String> params = new HashMap<>();
        params.put(PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        params.put(EXECUTION_NAME_PARAM, allureServiceConfig.get(EXECUTION_NAME_PARAM));
        params.put(EXECUTION_FROM_PARAM, allureServiceConfig.get(EXECUTION_FROM_PARAM));
        params.put(EXECUTION_TYPE_PARAM, allureServiceConfig.get(EXECUTION_TYPE_PARAM));
        Response response = restDriver.get(getQueryURL("/generate-report"), params);
        checkResponse(response, REPORT_NOT_GENERATED_MESSAGE);
    }

    /**
     * Sends the transfer container
     *
     * @param transferContainer Container with the files to be sent
     * @return Response from the service
     */
    private Response sendResults(ObjectNode transferContainer) {
        Map<String, String> params = new HashMap<>();
        params.put(PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        params.put(FORCE_PROJECT_CREATION_PARAM, allureServiceConfig.get(FORCE_PROJECT_CREATION_PARAM));
        return restDriver.post(getQueryURL("/send-results"), transferContainer.toString(), params);
    }

    /**
     * Cleans the results
     */
    public void cleanResults() {
        Response response = restDriver.get(getQueryURL("/clean-results"), PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        checkResponse(response, RESULT_NOT_DELETED_MESSAGE);
    }

    /**
     * Cleans the history
     */
    public void cleanHistory() {
        Response response = restDriver.get(getQueryURL("/clean-history"), PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        checkResponse(response, HISTORY_NOT_DELETED_MESSAGE);
    }

    /**
     * Checks if the project exists
     *
     * @return true if the project exists
     */
    public boolean existProject() {
        return restDriver.get(getQueryURL("/projects/" + allureServiceConfig.get(PROJECT_ID_PARAM))).getStatus() == 200;

    }

    /**
     * Gets the full URL including the host
     *
     * @param path Path to append
     * @return URL including the host
     */
    public String getQueryURL(String path) {
        return allureServiceConfig.get("host") + path;
    }

    /**
     * Closes the RestDriver
     */
    public void close() {
        restDriver.close();
    }

}