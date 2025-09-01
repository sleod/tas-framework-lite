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
 * TASRestClient is a client for making RESTful API calls using the provided TASRestDriver.
 * It provides methods to handle responses, including parsing JSON and storing files.
 */
@Getter
public class TASRestClient {

    private final TASRestDriver restDriver;

    /**
     * Constructor for TASRestClient.
     *
     * @param restDriver The TASRestDriver instance to be used for making REST calls.
     */
    public TASRestClient(TASRestDriver restDriver) {
        this.restDriver = restDriver;
    }

    /**
     * Cleans all parameters from the underlying REST driver.
     */
    public void cleanParams() {
        restDriver.cleanParams();
    }

    /**
     * Get the response as JsonNode. If the response is not successful, an exception is thrown with the provided error message.
     *
     * @param response     The Response object to be processed.
     * @param errorMessage The error message to be used in case of an unsuccessful response.
     * @return JsonNode representing the response body if successful.
     * @throws ExceptionBase if the response is not successful or if there is an error processing the JSON.
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
     * Closes the underlying REST driver, releasing any resources it may be holding.
     */
    public void close() {
        restDriver.close();
    }

    /**
     * Stores the InputStream from the Response into the specified target file.
     * If the response is not successful, an exception is thrown.
     *
     * @param response   The Response object containing the InputStream to be stored.
     * @param targetFile The target file where the InputStream will be written.
     * @throws ExceptionBase if there is an error during file writing or if the response is not successful.
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
     * Checks if the HTTP response status code indicates a successful request.
     *
     * @param response The Response object to be checked.
     * @return true if the status code is in the range [200, 230), false otherwise.
     */
    public static boolean isSuccessful(Response response) {
        return response.getStatus() >= 200 && response.getStatus() < 230;
    }

    /**
     * Checks the response for success and throws an exception with a custom error message if not successful.
     *
     * @param response     The Response object to be checked.
     * @param errorMessage The custom error message to be used in case of failure.
     * @throws ExceptionBase if the response is not successful.
     */
    public static void checkResponse(Response response, String errorMessage) {
        if (!isSuccessful(response)) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, errorMessage + response.getStatus() + System.lineSeparator() + response.readEntity(String.class));
        } else {
            debug("Rest Quer Successful: " + System.lineSeparator() + response);
        }
    }


}
