package ch.qa.testautomation.tas.rest.allure.connection;

import ch.qa.testautomation.tas.common.IOUtils.FileOperation;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.List;
import java.util.Map;

import static ch.qa.testautomation.tas.core.json.ObjectMapperSingleton.getObjectMapper;

/**
 * Stellt Methoden zur Verfügung, um benötigte Transfer Files
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
    public ObjectNode prepareFileTransferContainer(List<String> listOfFilesPathToSend) {
        ObjectMapper mapper = getObjectMapper();
        ObjectNode transferContainer = mapper.createObjectNode();
        ArrayNode results = mapper.createArrayNode();
        for (String path : listOfFilesPathToSend) {
            String encoded64Content = FileOperation.encodeFileToBase64(new File(path));
            //add result to array
            results.add(mapper.createObjectNode().put(CONTENT_BASE_64_NODE, encoded64Content)
                    .put(FILE_NAME_NODE, FileOperation.getFileName(path)));
        }
        return transferContainer.set(RESULTS_NODE, results);
    }

    public ObjectNode prepareFileTransferNode(Map<String, String> file4Upload) {
        ObjectMapper mapper = getObjectMapper();
        ObjectNode transferContainer = mapper.createObjectNode();
        ArrayNode results = mapper.createArrayNode();
        file4Upload.forEach((key, value) -> {
            //add result to array
            results.add(mapper.createObjectNode()
                    .put(CONTENT_BASE_64_NODE, value)
                    .put(FILE_NAME_NODE, key));
        });
        return transferContainer.set(RESULTS_NODE, results);
    }

}
