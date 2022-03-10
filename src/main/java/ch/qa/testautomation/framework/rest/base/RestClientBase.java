package ch.qa.testautomation.framework.rest.base;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.exception.JsonProcessException;
import ch.qa.testautomation.framework.intefaces.RestDriver;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;

import static ch.qa.testautomation.framework.core.json.ObjectMapperSingleton.getObjectMapper;

public class RestClientBase {

    public RestDriver getRestDriver() {
        return restDriver;
    }

    private final RestDriver restDriver;

    public RestClientBase(RestDriver restDriver) {
        this.restDriver = restDriver;
    }


    public JsonNode getResponseNode(Response response, String errorMessage) {
        ObjectMapper objectMapper = getObjectMapper();
        JsonNode responseNode;
        if (response.getStatus() == 200) {
            try {
                responseNode = objectMapper.readTree(response.readEntity(String.class));
            } catch (JsonProcessingException e) {
                throw new JsonProcessException(e);
            }
        } else {
            SystemLogger.debug("Status code: " + response.getStatus());
            throw new RuntimeException(errorMessage);
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
}
