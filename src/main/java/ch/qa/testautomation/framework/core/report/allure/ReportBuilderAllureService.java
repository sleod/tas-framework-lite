package ch.qa.testautomation.framework.core.report.allure;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.qa.testautomation.framework.rest.allure.connection.AllureRestClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Builder um Resultate dem Allure Web Service zu senden
 */
public class ReportBuilderAllureService {

    public static final String ALLURE_SERVICE_CONFIG = "allureServiceConfig";
    public static final String CONTENT_BASE_64_NODE = "content_base64";
    public static final String FILE_NAME_NODE = "file_name";
    public static final String RESULTS_NODE = "results";
    public static final String resultsDir = PropertyResolver.getAllureResultsDir() + "upload/";
    private static final String ATTACHMENT = "-attachment.";
    private AllureRestClient restClient;

    /**
     * Erzeugt den Report auf dem Allure Webservice
     */
    public void generateAllureReportOnService() {

        try {
            new File(resultsDir).mkdirs();

            modifyResultFiles();

            restClient = initAllureRestClient();

            restClient.uploadAllureResultFiles();

            restClient.generateReportOnService();

        } finally {
            FileOperation.deleteFolder(resultsDir);
            closeClient();
        }
    }

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

        JsonNode config = JSONContainerFactory.getConfig(PropertyResolver.getReportServiceRunnerConfigFile());
        return new AllureRestClient((ObjectNode) config.get(ALLURE_SERVICE_CONFIG));
    }

    /**
     * Aendert die Pfade in den Results Files und ermittelt
     * dann die dazugehoerigen Attachments Files
     */
    protected void modifyResultFiles() {

        Map<String, String> changedAttachmentSourcePath;

        try {
            //List<String> resultFilesPaths = JSONContainerFactory.getAllureResults();
            //change: list only result files of current run
            List<Path> resultFilesPaths = JSONContainerFactory.listCurrentAllureResults();
            for (Path resultFilePath : resultFilesPaths) {
                JsonNode allureResultObject = JSONContainerFactory.getAllureResultObject(resultFilePath);
                changedAttachmentSourcePath = changeAttachmentsPathInResultFile(allureResultObject);
                //Geänderte Result File mit neuen Pfaden im Ordner allure-result ueberschreiben
                String newFilePath = resultsDir + resultFilePath.getFileName();
                FileOperation.writeBytesToFile(allureResultObject.toString().getBytes(), new File(newFilePath));
                collectAttachmentsFiles(changedAttachmentSourcePath);
                changedAttachmentSourcePath.clear();
            }

        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Kopiert die benoetigten Attachments Files und benennt sie um
     *
     * @param changedSourcePath changed source path
     */
    protected void collectAttachmentsFiles(Map<String, String> changedSourcePath) {
        changedSourcePath.put(PropertyResolver.getAllureResultsDir() + "environment.properties", resultsDir + "environment.properties");
        changedSourcePath.forEach((sourcePath, targetPath) -> {
            try {
                FileUtils.copyFile(FileUtils.getFile(sourcePath), FileUtils.getFile(resultsDir + targetPath));
            } catch (IOException e) {
                throw new RuntimeException(e);
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

        //Ueber den Inhalt der aeusseren attachments iterieren
        replaceSourcePathWithUUID(changedSourcePathList, allureResultObject);

        //Ueber den Inhalt in den Steps iterieren
        ArrayNode steps = (ArrayNode) allureResultObject.get("steps");
        for (JsonNode stepObject : steps) {

            ObjectNode step = (ObjectNode) stepObject;
            replaceSourcePathWithUUID(changedSourcePathList, step);
        }
        return changedSourcePathList;
    }

    /**
     * Such im JSON Object nach attachments Nodes und aendert deren
     * source path durch eine UUID
     *
     * @param changedSourcePath List welche Pfade geaendert wurden
     * @param jsonObject        Inhalt des -result.json welche verändert wird
     */
    protected void replaceSourcePathWithUUID(Map<String, String> changedSourcePath, JsonNode jsonObject) {

        String attachmentNode = "attachments";
        String sourceNode = "source";

        ArrayNode attachments = (ArrayNode) jsonObject.get(attachmentNode);

        for (JsonNode attachmentObject : attachments) {

            ObjectNode attachment = (ObjectNode) attachmentObject;
            String sourceValue = attachment.get(sourceNode).asText();

            //Prüfen ob bereits eine UUID-attachment. existiert
            if (!sourceValue.isEmpty() && !FileOperation.startWithUUID(sourceValue)) {

                String extension = FileOperation.getFileNameExtension(sourceValue);
                String uuidFilePath = UUID.randomUUID() + "-attachment." + extension;
                attachment.put(sourceNode, uuidFilePath);
                changedSourcePath.put(sourceValue, uuidFilePath);
            }
        }
    }
}