package ch.qa.testautomation.framework.rest.base;

import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.framework.core.json.ObjectMapperSingleton.getObjectMapper;

public class RestClientBase {

    protected final RestDriverBase restDriver;

    public RestClientBase(RestDriverBase restDriver) {
        this.restDriver = restDriver;
    }
    public RestDriverBase getRestDriver() {
        return restDriver;
    }

    public static JsonNode getResponseNode(Response response, String errorMessage) {
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode responseNode;
        debug(response.toString());
        if (isSuccessful(response)) {
            try {
                responseNode = objectMapper.readTree(response.readEntity(String.class));
            } catch (JsonProcessingException ex) {
                throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, ex, "Exception while read tree of Json!");
            }
        } else {
            debug("Status code: " + response.getStatus());
            debug(response.readEntity(String.class));
            throw new ApollonBaseException(ApollonErrorKeys.CUSTOM_MESSAGE, errorMessage);
        }
        return responseNode;
    }

    public void close() {
        restDriver.close();
    }

    public static String encodeUrlPath(String pathSegment) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < pathSegment.length(); i++) {
            final char c = pathSegment.charAt(i);

            if (((c >= 'A') && (c <= 'Z')) || ((c >= 'a') && (c <= 'z')) || ((c >= '0') && (c <= '9'))
                    || (c == '-') || (c == '.') || (c == '_') || (c == '~')) {
                sb.append(c);
            } else {
                final byte[] bytes = String.valueOf(c).getBytes(StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append('%') //
                            .append(Integer.toHexString((b >> 4) & 0xf)) //
                            .append(Integer.toHexString(b & 0xf));
                }
            }
        }
        return sb.toString();
    }

    public static void storeStreamIntoFile(Response response, File targetFile) {
        if (isSuccessful(response)) {
            targetFile.getParentFile().mkdirs();
            info("Write to target: " + targetFile.getAbsolutePath());
            try {
                Files.copy(response.readEntity(InputStream.class), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                info("Storage File Successful: " + targetFile.getName());
            } catch (IOException ex) {
                throw new ApollonBaseException(ApollonErrorKeys.IOEXCEPTION_BY_WRITING, ex, targetFile.getPath());
            }
        } else {
            debug(response.readEntity(String.class));
            throw new ApollonBaseException(ApollonErrorKeys.FAIL_TO_DOWNLOAD_FILE, targetFile.getName(),response.getStatus());
        }
    }

    private static void info(String s) {
    }

    public static boolean isSuccessful(Response response) {
        return response.getStatus() >= 200 && response.getStatus() < 230;
    }

}
