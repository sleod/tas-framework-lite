package ch.qa.testautomation.framework.rest.allure.connection;

import ch.qa.testautomation.framework.common.IOUtils.FileOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;

import static ch.qa.testautomation.framework.core.json.ObjectMapperSingleton.getObjectMapper;

/**
 * Stellt Methoden zur Verfügung um benötigte Transfer Files
 * für den Service zu erstellen
 */
public class TransferFileBuilder {

    public static final String CONTENT_BASE_64_NODE = "content_base64";
    public static final String FILE_NAME_NODE = "file_name";
    public static final String RESULTS_NODE = "results";

    /**
     * Sammelt die benoetigten JSON Files aus dem allure-result Ordner und erstellt ein
     * Transfers Object json
     *
     * @return JSON mit encoded Base64 String
     */
    public ObjectNode prepareFileTransferContainer(List<String> listOfFilesPathToSend) {
        ObjectMapper mapper = getObjectMapper();
        ObjectNode transferContainer = mapper.createObjectNode();
        ArrayNode results = mapper.createArrayNode();
        for (String path : listOfFilesPathToSend) {
            String encoded64Content = FileOperation.encodeFileToBase64(new File(path));
            ObjectNode fileData = mapper.createObjectNode();
            fileData.put(CONTENT_BASE_64_NODE, encoded64Content);
            //Die Methode JSONContainerFactory.getAllureResults() liefert zwei // zurück, daher diese entfernen
            fileData.put(FILE_NAME_NODE, FileOperation.getFileName(path));
            results.add(fileData);
        }

        transferContainer.set(RESULTS_NODE, results);

        return transferContainer;
    }
}
