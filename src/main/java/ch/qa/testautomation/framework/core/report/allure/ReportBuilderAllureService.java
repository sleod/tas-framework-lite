package ch.qa.testautomation.framework.core.report.allure;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.rest.allure.connection.AllureRestClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Builder um Resultate dem Allure Web Service zu senden
 */
public class ReportBuilderAllureService {

    public static final String ALLURE_SERVICE_CONFIG = "allureServiceConfig";
    public static final String resultsDir = PropertyResolver.getAllureResultsDir() + "upload_" + Thread.currentThread().getName() + "/";
    private AllureRestClient restClient;

    public void uploadAllureResults() {
        try {
            new File(resultsDir).mkdirs();
            modifyResultFiles();
            restClient = initAllureRestClient();
            restClient.uploadAllureResultFiles();
        } finally {
            FileOperation.deleteFolder(resultsDir);
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
    protected void modifyResultFiles() {
        Map<String, String> changedAttachmentSourcePath;
        //change: list only result files of current run
        List<Path> resultFilesPaths = JSONContainerFactory.listCurrentAllureResults();
        for (Path resultFilePath : resultFilesPaths) {
            JsonNode allureResultObject = JSONContainerFactory.getAllureResultObject(resultFilePath);
            changedAttachmentSourcePath = changeAttachmentsPathInResultFile(allureResultObject);
            //Geänderte Result File mit neuen Pfaden im Ordner allure-result überschreiben
            String newFilePath = resultsDir + resultFilePath.getFileName();
            FileOperation.writeStringToFile(allureResultObject.toString(), newFilePath);
            collectAttachmentsFiles(changedAttachmentSourcePath);
            changedAttachmentSourcePath.clear();
        }

    }

    /**
     * Kopiert die benötigten Attachments Files und benennt sie um
     *
     * @param changedSourcePath changed source path
     */
    protected void collectAttachmentsFiles(Map<String, String> changedSourcePath) {
        changedSourcePath.put(PropertyResolver.getAllureResultsDir() + "environment.properties", resultsDir + "environment.properties");
        changedSourcePath.forEach((sourcePath, targetPath) -> {
            try {
                FileUtils.copyFile(FileUtils.getFile(sourcePath), FileUtils.getFile(resultsDir + targetPath));
            } catch (IOException e) {
                throw new ApollonBaseException(e);
            }
        });
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