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
 * Liefert Methoden, um mit dem Allure Service zu kommunizieren
 */
public class AllureRestClient {

    public static final String RESULT_NOT_SEND_MESSAGE = "Die Resultate konnten nicht übermittelt werden. Response Code: ";
    public static final String REPORT_NOT_GENERATED_MESSAGE = "Der Report konnte nicht generiert werden. Response Code: ";
    public static final String HISTORY_NOT_DELETED_MESSAGE = "Die History konnte nicht nicht gelöscht werden. Response Code: ";
    public static final String RESULT_NOT_DELETED_MESSAGE = "Die Resultate konnten nicht nicht gelöscht werden. Response Code: ";
    public static final String PROJECT_ID_PARAM = "project_id";
    public static final String PROJECT_ID_IS_NOT_DEFINED = "Der Parameter " + PROJECT_ID_PARAM + " in der File reportServiceRunnerConfig.json darf nicht leer sein";
    public static final String EXECUTION_NAME_PARAM = "execution_name";
    public static final String EXECUTION_FROM_PARAM = "execution_from";
    public static final String EXECUTION_TYPE_PARAM = "execution_type";
    public static final String FORCE_PROJECT_CREATION_PARAM = "force_project_creation";
    private final TransferFileBuilder transferFileBuilder;
    private final Map<String, String> allureServiceConfig;

    private final SimpleRestDriver restDriver;


    public AllureRestClient(Map<String, String> config) {
        restDriver = new SimpleRestDriver();
        if (config.get(PROJECT_ID_PARAM).isEmpty()) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, PROJECT_ID_IS_NOT_DEFINED);
        }
        this.allureServiceConfig = config;
        this.transferFileBuilder = new TransferFileBuilder();
    }

    /**
     * Ladet den Inhalt der result-json und attachments Files zum Allure Service hoch
     */
    public void uploadAllureResultFiles(Map<String, String> file4Upload) {
        ObjectNode transferContainer = transferFileBuilder.prepareFileTransferNode(file4Upload);
        Response response = sendResults(transferContainer);
        checkResponse(response, RESULT_NOT_SEND_MESSAGE);
    }

    /**
     * Ladet extra files zum Allure Service hoch
     */
    public void uploadExtraFiles() {
        List<String> resultsPaths = JSONContainerFactory.getAllureExtra4Upload();
        ObjectNode transferContainer = transferFileBuilder.prepareFileTransferContainer(resultsPaths);
        debug("Find allure extra files for upload: " + resultsPaths.stream().collect(Collectors.joining(System.lineSeparator())));
        Response response = sendResults(transferContainer);
        checkResponse(response, RESULT_NOT_SEND_MESSAGE);
    }

    /**
     * Triggert den Befehl, um den Report zu erstellen
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
     * Sendet den Transfer Container
     */
    private Response sendResults(ObjectNode transferContainer) {
        Map<String, String> params = new HashMap<>();
        params.put(PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        params.put(FORCE_PROJECT_CREATION_PARAM, allureServiceConfig.get(FORCE_PROJECT_CREATION_PARAM));
        return restDriver.post(getQueryURL("/send-results"), transferContainer.toString(), params);
    }

    /**
     * Löscht die aktuellen Resultate
     */
    public void cleanResults() {
        Response response = restDriver.get(getQueryURL("/clean-results"), PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        checkResponse(response, RESULT_NOT_DELETED_MESSAGE);
    }

    /**
     * Löscht die Historie
     */
    public void cleanHistory() {
        Response response = restDriver.get(getQueryURL("/clean-history"), PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        checkResponse(response, HISTORY_NOT_DELETED_MESSAGE);
    }

    /**
     * Abfrage ob Projekt existiert
     */
    public boolean existProject() {
        return restDriver.get(getQueryURL("/projects/" + allureServiceConfig.get(PROJECT_ID_PARAM))).getStatus() == 200;

    }

    public String getQueryURL(String path) {
        return allureServiceConfig.get("host") + path;
    }

    public void close() {
        restDriver.close();
    }

}
