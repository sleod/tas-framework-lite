package ch.qa.testautomation.tas.rest.base;

import ch.qa.testautomation.tas.core.json.ObjectMapperSingleton;
import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.tas.common.logging.SystemLogger.info;


public class RestClientBase {

    private final RestDriverBase restDriver;

    public RestClientBase(RestDriverBase restDriver) {
        this.restDriver = restDriver;
    }

    public RestDriverBase getRestDriver() {
        return restDriver;
    }

    public void cleanParams() {
        restDriver.cleanParams();
    }

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

    public void close() {
        restDriver.close();
    }


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

    public static boolean isSuccessful(Response response) {
        return response.getStatus() >= 200 && response.getStatus() < 230;
    }

    public static void checkResponse(Response response, String errorMessage) {
        if (!isSuccessful(response)) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, errorMessage + response.getStatus() + System.lineSeparator() + response.readEntity(String.class));
        } else {
            debug("Rest Quer Successful: " + System.lineSeparator() + response);
        }
    }


}
