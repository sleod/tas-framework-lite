package ch.qa.testautomation.framework.rest.allure.connection;

import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import ch.qa.testautomation.framework.rest.base.RestClientBase;
import ch.qa.testautomation.framework.rest.base.RestDriverBase;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.qa.testautomation.framework.core.report.allure.ReportBuilderAllureService.resultsDir;

/**
 * Liefert Methoden, um mit dem Allure Service zu kommunizieren
 */
public class AllureRestClient extends RestClientBase {

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

    public AllureRestClient(Map<String, String> config) {
        super(new RestDriverBase(config.get("host")));
        if (config.get(PROJECT_ID_PARAM).isEmpty()) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, PROJECT_ID_IS_NOT_DEFINED);
        }
        this.allureServiceConfig = config;
        this.transferFileBuilder = new TransferFileBuilder();
    }

    /**
     * Ladet den Inhalt der result-json und attachments Files zum Allure Service hoch
     */
    public void uploadAllureResultFiles() {
        List<String> resultsPaths = JSONContainerFactory.getAllureResults4Upload(resultsDir);
        ObjectNode transferContainer = transferFileBuilder.prepareFileTransferContainer(resultsPaths);
        if (existProject()) {
            cleanResults();
        }
        Response response = sendResults(transferContainer);
        checkResponse(response, RESULT_NOT_SEND_MESSAGE);
    }

    /**
     * Triggert den Befehl um den Report zu erstellen
     */
    public void generateReportOnService() {
        Map<String, String> params = new HashMap<>();
        params.put(PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        params.put(EXECUTION_NAME_PARAM, allureServiceConfig.get(EXECUTION_NAME_PARAM));
        params.put(EXECUTION_FROM_PARAM, allureServiceConfig.get(EXECUTION_FROM_PARAM));
        params.put(EXECUTION_TYPE_PARAM, allureServiceConfig.get(EXECUTION_TYPE_PARAM));
        Response response = restDriver.get("/generate-report", params);
        checkResponse(response, REPORT_NOT_GENERATED_MESSAGE);
    }

    /**
     * Sendet den Transfer Container
     */
    private Response sendResults(ObjectNode transferContainer) {

        Map<String, String> params = new HashMap<>();
        params.put(PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        params.put(FORCE_PROJECT_CREATION_PARAM, allureServiceConfig.get(FORCE_PROJECT_CREATION_PARAM));
        return restDriver.post("/send-results", transferContainer.toString(), params);
    }

    /**
     * Löscht die aktuellen Resultate
     */
    public void cleanResults() {
        Response response = restDriver.get("/clean-results", PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        checkResponse(response, RESULT_NOT_DELETED_MESSAGE);
    }

    /**
     * Löscht die Historie
     */
    public void cleanHistory() {
        Response response = restDriver.get("/clean-history", PROJECT_ID_PARAM, allureServiceConfig.get(PROJECT_ID_PARAM));
        checkResponse(response, HISTORY_NOT_DELETED_MESSAGE);
    }

    /**
     * Abfrage ob Projekt existiert
     */
    public boolean existProject() {
        return restDriver.get("/projects/" + allureServiceConfig.get(PROJECT_ID_PARAM)).getStatus() == 200;

    }

    private void checkResponse(Response response, String errorMessage) {
        if (!isSuccessful(response)) {
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, errorMessage + response.getStatus() + System.lineSeparator() + response.readEntity(String.class));
        }
    }
}
