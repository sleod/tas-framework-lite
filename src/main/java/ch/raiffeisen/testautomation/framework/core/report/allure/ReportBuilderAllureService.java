package ch.raiffeisen.testautomation.framework.core.report.allure;

import ch.raiffeisen.testautomation.framework.common.IOUtils.FileOperation;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import ch.raiffeisen.testautomation.framework.rest.allure.connection.AllureRestClient;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
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

    /**
     *Erzeugt den Report auf dem Allure Webservice
     */
    public void generateAllureReportOnService() {

        AllureRestClient restClient = null;
        try {

            modifyResultFiles();

            restClient = initAllureRestClient();

            restClient.uploadAllureResultFiles();

            restClient.generateReportOnService();

        } finally {

            FileOperation.deleteFolder(PropertyResolver.getAllureResultsDir());

            if (restClient != null) {
                restClient.close();
            }
        }
    }

    protected AllureRestClient initAllureRestClient() {

        JSONObject config = JSONContainerFactory.getConfig(PropertyResolver.getReportServiceRunnerConfigFile());
        return new AllureRestClient((JSONObject) config.get(ALLURE_SERVICE_CONFIG));
    }

    /**
     * Aendert die Pfade in den Results Files und ermittelt
     * dann die dazugehoerigen Attachments Files
     */
    protected void modifyResultFiles() {

        Map<String, String> changedAttachmentSourcePath = null;

        try {
            List<String> resultFilesPaths = JSONContainerFactory.getAllureResults();

            for (String resultFilePath : resultFilesPaths) {

                JSONObject allureResultObject = JSONContainerFactory.getAllureResultObject(Paths.get(resultFilePath));
                changedAttachmentSourcePath = changeAttachmentsPathInResultFile(allureResultObject);
                //Geaenderte Result File mit neuen Pfaden im Ordner allure-result ueberschreiben
                FileOperation.writeBytesToFile(allureResultObject.toString().getBytes(), new File(resultFilePath));

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
     * @param changedSourcePath
     */
    protected void collectAttachmentsFiles(Map<String, String> changedSourcePath) {

        changedSourcePath.forEach((sourcePath, targetPath) -> {

            try {
                String targetDir = PropertyResolver.getAllureResultsDir() + targetPath;
                FileUtils.copyFile(FileUtils.getFile(sourcePath), FileUtils.getFile(targetDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Iteriert durch die -result.json, beim Node 'attachments' wird der Pfad ausgelesen und
     * durch eine UUID ersetzt
     *
     * @param
     */
    protected Map<String, String> changeAttachmentsPathInResultFile(JSONObject allureResultObject) {

        Map<String, String> changedSourcePathList = new HashMap<>();

        //Ueber den Inhalt der aeusseren attachments iterieren
        replaceSourcePathWithUUID(changedSourcePathList, allureResultObject);

        //Ueber den Inhalt in den Steps iterieren
        JSONArray steps = (JSONArray) allureResultObject.get("steps");
        for (Object stepObject : steps) {

            JSONObject step = (JSONObject) stepObject;
            replaceSourcePathWithUUID(changedSourcePathList, step);
        }
        return changedSourcePathList;
    }

    /**
     * Such im JSON Object nach attachments Nodes und aendert deren
     * source path durch eine UUID
     *
     * @param changedSourcePath List welche Pfade geaendert wurden
     * @param jsonObject        Inhalt des -result.json
     */
    protected void replaceSourcePathWithUUID(Map<String, String> changedSourcePath, JSONObject jsonObject) {

        String attachmentNode = "attachments";
        String sourceNode = "source";

        JSONArray attachments = (JSONArray) jsonObject.get(attachmentNode);

        for (Object attachmentObject : attachments) {

            JSONObject attachment = (JSONObject) attachmentObject;
            String sourceValue = (String) attachment.get(sourceNode);

            if (!sourceValue.isEmpty() && !FileOperation.isUUID(sourceValue.substring(0, sourceValue.lastIndexOf("-")))) {

                String extension = FileOperation.getFileNameExtension(sourceValue);
                String uuidFilePath = UUID.randomUUID() + "-attachment." + extension;
                attachment.put(sourceNode, uuidFilePath);
                changedSourcePath.put(sourceValue, uuidFilePath);
            }
        }
    }
}