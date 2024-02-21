package ch.qa.testautomation.tas.core.report.allure;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.tas.rest.allure.AllureRestClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;


/**
 * Builder um Resultate dem Allure Web Service zu senden
 */
public class ReportBuilderAllureService {

    public final String ALLURE_SERVICE_CONFIG = "allureServiceConfig";
    public final Map<String, String> file4Upload = new LinkedHashMap<>();
    private AllureRestClient restClient;

    public void uploadAllureResults(List<String> filePath) {
        try {
            modifyResultFiles(filePath);
            restClient = initAllureRestClient();
            restClient.uploadAllureResultFiles(file4Upload);
        } finally {
            closeClient();
        }
    }

    public void uploadAllureExtra() {
        try {
            restClient = initAllureRestClient();
            restClient.uploadExtraFiles();
        } finally {
            closeClient();
        }
    }

    public void closeClient() {
        if (restClient != null) {
            restClient.close();
            restClient = null;
        }
    }

    public void generateReportOnService() {
        restClient = initAllureRestClient();
        restClient.generateReportOnService();
        closeClient();
    }

    public void cleanResultsByPresent() {
        restClient = initAllureRestClient();
        if (restClient.existProject()) {
            restClient.cleanResults();
        }
        closeClient();
    }

    protected AllureRestClient initAllureRestClient() {
        if (Objects.isNull(restClient)) {
            JsonNode config = JSONContainerFactory.getConfig(PropertyResolver.getReportServiceRunnerConfigFile());
            Map<String, String> result = new ObjectMapper().convertValue(config.get(ALLURE_SERVICE_CONFIG), new TypeReference<>() {
            });
            return new AllureRestClient(result);
        } else return restClient;
    }

    /**
     * Aendert die Pfade in den Results Files und ermittelt
     * dann die dazugehoerigen Attachments Files
     */
    protected void modifyResultFiles(List<String> resultFilesPaths) {
        Map<String, String> changedAttachmentSourcePath;
        //change: list only result files of current run
        for (String resultFilePath : resultFilesPaths) {
            File file = new File(resultFilePath);
            JsonNode allureResultObject = JSONContainerFactory.getAllureResultObject(file.toPath());
            changedAttachmentSourcePath = changeAttachmentsPathInResultFile(allureResultObject);
            //Geänderte Result File mit neuen Pfaden im Ordner allure-result überschreiben
            file4Upload.put(file.getName(), PropertyResolver.encodeBase64(allureResultObject.toString()));
            changedAttachmentSourcePath.forEach((sourcePath, targetPath) ->
                    file4Upload.put(FileOperation.getFileName(targetPath), FileOperation.encodeFileToBase64(Paths.get(sourcePath).toFile())));
        }
    }

    /**
     * Iteriert durch die -result.json, beim Node 'attachments' wird der Pfad ausgelesen und
     * durch eine UUID ersetzt
     *
     * @param allureResultObject allure result object
     */
    protected Map<String, String> changeAttachmentsPathInResultFile(JsonNode allureResultObject) {

        Map<String, String> changedSourcePathList = new HashMap<>();

        //über den Inhalt der äusseren attachments iterieren
        replaceSourcePathWithUUID(changedSourcePathList, allureResultObject);

        //über den Inhalt in den Steps iterieren
        ArrayNode steps = (ArrayNode) allureResultObject.get("steps");
        for (JsonNode stepObject : steps) {
            ObjectNode step = (ObjectNode) stepObject;
            replaceSourcePathWithUUID(changedSourcePathList, step);
        }
        return changedSourcePathList;
    }

    /**
     * Such im JSON Object nach attachments Nodes und ändert deren
     * source path durch eine UUID
     *
     * @param changedSourcePath List welche Pfade geändert wurden
     * @param jsonObject        Inhalt des -result.json welche verändert wird
     */
    public void replaceSourcePathWithUUID(Map<String, String> changedSourcePath, JsonNode jsonObject) {

        String sourceNode = "source";

        ArrayNode attachments = (ArrayNode) jsonObject.get("attachments");

        for (JsonNode attachmentObject : attachments) {

            ObjectNode attachment = (ObjectNode) attachmentObject;
            String sourceValue = attachment.get(sourceNode).asText();

            //Prüfen, ob bereits eine UUID-attachment. existiert
            if (!sourceValue.isEmpty() && !FileOperation.startWithUUID(sourceValue)) {
                String extension = FileOperation.getFileNameExtension(sourceValue);
                String uuidFilePath = UUID.randomUUID() + "-attachment." + extension;
                attachment.put(sourceNode, uuidFilePath);
                changedSourcePath.put(sourceValue, uuidFilePath);
            }
        }
    }
}