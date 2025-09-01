package io.github.sleod.tas.rest.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;
import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import jakarta.ws.rs.core.Response;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static io.github.sleod.tas.common.logging.SystemLogger.debug;
import static io.github.sleod.tas.common.logging.SystemLogger.info;


/**
 * Base class for REST clients providing common functionality for handling REST responses and errors.
 */
@Getter
public class RestClientBase {

    private final RestDriverBase restDriver;

    public RestClientBase(RestDriverBase restDriver) {
        this.restDriver = restDriver;
    }

    public void cleanParams() {
        restDriver.cleanParams();
    }

    /**
     * Processes the REST response and returns a JsonNode if the response is successful.
     * If the response indicates an error, an ExceptionBase is thrown with the provided error message.
     *
     * @param response     The REST response to process.
     * @param errorMessage The error message to use if the response indicates an error.
     * @return A JsonNode representing the response body if successful.
     * @throws ExceptionBase If the response indicates an error or if there is an issue processing the JSON.
     */
    public JsonNode getResponseNode(Response response, String errorMessage) {
        debug("Status code: " + response.getStatus());
        String body = response.readEntity(String.class);
        debug(body);
        if (isSuccessful(response)) {
            try {
                response.close();
                return ObjectMapperSingleton.mapper().readTree(body);
            } catch (JsonProcessingException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, ex, "Exception while read tree of Json!");
            }
        } else {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, errorMessage);
        }
    }

    /**
     * Closes the underlying REST driver, releasing any associated resources.
     */
    public void close() {
        restDriver.close();
    }

    /**
     * Stores the content of the REST response into the specified target file.
     * If the response is successful, the content is written to the file.
     * If the response indicates an error, an ExceptionBase is thrown with details.
     *
     * @param response   The REST response containing the content to store.
     * @param targetFile The target file where the content should be stored.
     * @throws ExceptionBase If there is an issue writing to the file or if the response indicates an error.
     */
    protected void storeStreamIntoFile(Response response, File targetFile) {
        if (isSuccessful(response)) {
            targetFile.getParentFile().mkdirs();
            info("Write to target: " + targetFile.getAbsolutePath());
            try {
                Files.copy(response.readEntity(InputStream.class), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                info("Storage File Successful: " + targetFile.getName());
            } catch (IOException ex) {
                throw new ExceptionBase(ExceptionErrorKeys.IOEXCEPTION_BY_WRITING, ex, targetFile.getPath());
            }
        } else {
            debug(response.readEntity(String.class));
            throw new ExceptionBase(ExceptionErrorKeys.FAIL_TO_DOWNLOAD_FILE, targetFile.getName(), response.getStatus());
        }
    }

    /**
     * Checks if the REST response is successful based on its status code.
     * A response is considered successful if its status code is in the range [200, 230).
     *
     * @param response The REST response to check.
     * @return true if the response is successful; false otherwise.
     */
    public static boolean isSuccessful(Response response) {
        return response.getStatus() >= 200 && response.getStatus() < 230;
    }

    /**
     * Checks the REST response and throws an ExceptionBase with the provided error message if the response is not successful.
     * If the response is successful, a debug message is logged.
     *
     * @param response     The REST response to check.
     * @param errorMessage The error message to use if the response indicates an error.
     * @throws ExceptionBase If the response indicates an error.
     */
    public static void checkResponse(Response response, String errorMessage) {
        if (!isSuccessful(response)) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, errorMessage + response.getStatus() + System.lineSeparator() + response.readEntity(String.class));
        } else {
            debug("Rest Quer Successful: " + System.lineSeparator() + response);
        }
    }

}
