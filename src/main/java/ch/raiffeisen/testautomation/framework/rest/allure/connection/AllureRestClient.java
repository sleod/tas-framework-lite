package ch.raiffeisen.testautomation.framework.rest.allure.connection;

import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import net.sf.json.JSONObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.raiffeisen.testautomation.framework.core.report.allure.ReportBuilderAllureService.resultsDir;

/**
 * Liefert Methoden um mit dem Allure Service zu kommunizieren
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
    private final AllureServiceConnector allureConnector;
    private final JSONObject allureServiceConfig;

    public AllureRestClient(JSONObject allureServiceConfig) {

        checkExistsProjectId(allureServiceConfig);

        this.allureServiceConfig = allureServiceConfig;
        this.allureConnector = new AllureServiceConnector(allureServiceConfig);
        this.transferFileBuilder = new TransferFileBuilder();
    }

    private void checkExistsProjectId(JSONObject allureServiceConfig) {

        if ("".equals( allureServiceConfig.get(PROJECT_ID_PARAM))) {
            throw new IllegalArgumentException(PROJECT_ID_IS_NOT_DEFINED);
        }
    }

    public void close() {

        allureConnector.close();
    }

    /**
     * Ladet den Inhalt der result-json und attachments Files zum Allure Service hoch
     */
    public void uploadAllureResultFiles() {

        List<String> resultsPaths = JSONContainerFactory.getAllureResults4Upload(resultsDir);

        JSONObject transferContainer = transferFileBuilder.prepareFileTransferContainer(resultsPaths);

        Response response = sendResults(transferContainer);

        checkResponse(response, RESULT_NOT_SEND_MESSAGE);
    }

    /**
     * Triggert den Befehl um den Report zu erstellen
     */
    public void generateReportOnService() {

        Map<String, String> params = new HashMap<>();
        params.put(PROJECT_ID_PARAM, (String) allureServiceConfig.get(PROJECT_ID_PARAM));
        params.put(EXECUTION_NAME_PARAM, (String) allureServiceConfig.get(EXECUTION_NAME_PARAM));
        params.put(EXECUTION_FROM_PARAM, (String) allureServiceConfig.get(EXECUTION_FROM_PARAM));
        params.put(EXECUTION_TYPE_PARAM, (String) allureServiceConfig.get(EXECUTION_TYPE_PARAM));

        Response response = allureConnector.get("/generate-report", params);

        checkResponse(response, REPORT_NOT_GENERATED_MESSAGE);
    }

    /**
     * Sendet den Transfer Container
     *
     * @param transferContainer
     */
    private Response sendResults(JSONObject transferContainer) {

        Map<String, String> params = new HashMap<>();
        params.put(PROJECT_ID_PARAM, (String) allureServiceConfig.get(PROJECT_ID_PARAM));
        params.put(FORCE_PROJECT_CREATION_PARAM, (String) allureServiceConfig.get(FORCE_PROJECT_CREATION_PARAM));

        return allureConnector.post("/send-results", transferContainer.toString(), params);
    }

    /**
     * Löscht die aktuellen Resultate
     */
    public void cleanResults() {

        Response response = allureConnector.get("/clean-results", PROJECT_ID_PARAM, (String) allureServiceConfig.get(PROJECT_ID_PARAM));

        checkResponse(response, RESULT_NOT_DELETED_MESSAGE);
    }

    /**
     * Löscht die Historie
     */
    public void cleanHistory() {

        Response response = allureConnector.get("/clean-history", PROJECT_ID_PARAM, (String) allureServiceConfig.get(PROJECT_ID_PARAM));

        checkResponse(response, HISTORY_NOT_DELETED_MESSAGE);
    }

    private void checkResponse(Response response, String errorMessage) {

        if (response.getStatus() != 200) {

            throw new WebApplicationException(errorMessage + response.getStatus() + System.lineSeparator() + response.readEntity(String.class));
        }
    }
}
