package ch.raiffeisen.testautomation.framework.rest.allure.connection;

import ch.raiffeisen.testautomation.framework.common.IOUtils.FileOperation;
import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.util.List;

/**
 * Stellt Methoden zur Verfügung um benötigte Transfer Files
 * für den Service zu erstellen
 */
public class TransferFileBuilder {

    public static final String CONTENT_BASE_64_NODE = "content_base64";
    public static final String FILE_NAME_NODE = "file_name";
    public static final String RESULTS_NODE = "results";

    /**
     * Sammelt die benötigten JSON Files aus dem allure-result Ordner und erstellt ein
     * Transfers Object json
     *
     * @return JSON mit encoded Base64 String
     */
    @SuppressWarnings({"unchecked"})
    public JSONObject prepareFileTransferContainer( List<String> listOfFilesPathToSend) {

        JSONObject transferContainer = new JSONObject();
        JSONArray results = new JSONArray();

        for (String path : listOfFilesPathToSend) {

            String encoded64Content = FileOperation.encodeFileToBase64(new File(path));
            JSONObject fileData = new JSONObject();
            fileData.put(CONTENT_BASE_64_NODE, encoded64Content);
            //Die Methode JSONContainerFactory.getAllureResults() liefert zwei // zurück, daher diese entfernen
            fileData.put(FILE_NAME_NODE, FileOperation.getFileName(path));

            results.add(fileData);
        }

        transferContainer.put(RESULTS_NODE, results);

        return transferContainer;
    }
}
