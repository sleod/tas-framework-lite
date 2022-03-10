package ch.qa.testautomation.framework.rest.allure.connection;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.intefaces.RestDriver;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;

public class AllureServiceConnector implements RestDriver {

    private WebTarget webTarget;
    private Response response;
    private Client client;
    private final String host;
    private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

    /**
     * Constructor with config in map
     *
     * @param config map of configs
     */
    public AllureServiceConnector(Map<String, String> config) {

        this.host = config.get("host");
        initialize();
    }

    @Override
    public void initialize() {

        if (webTarget == null) {

            this.client = ClientBuilder.newClient();
            this.connect();
        }
    }

    @Override
    public Response get(String path) {
        trace("Request GET: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .get();
        return response;
    }

    @Override
    public Response get(String path, String key, String value) {

        trace("Request Get: " + path + "\nWith Query: " + key + "=" + value);
        response = webTarget.path(path)
                .queryParam(key, value)
                .request(mediaType)
                .get();
        return response;
    }

    @Override
    public Response get(String path, Map<String, String> params) {

        trace("Request Get: " + path);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            webTarget = webTarget.queryParam(key, value);
            trace("Query: " + key + "=" + value);
        }
        response = webTarget.path(path)
                .request(mediaType)
                .get();

        return response;
    }

    @Override
    public void close() {
        webTarget = null;
        client.close();
    }

    @Override
    public void connect() {
        this.webTarget = client.target(host);
    }

    @Override
    public String getCookies() {
        return null;
    }

    @Override
    public Object get(String path, String query) {
        return null;
    }

    @Override
    public Object post(String path, String payload) {
        return null;
    }

    public Response post(String path, String payload,Map<String, String> params) {
        trace("Request POST: " + path);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            webTarget = webTarget.queryParam(key, value);
            SystemLogger.info("Query: " + key + "=" + value);

        }
        response = webTarget.path(path).request(mediaType)
                .post(Entity.entity(payload, mediaType));

        return response;
    }

    @Override
    public Object put(String path, String payload) {
        return null;
    }

    @Override
    public Object delete(String path) {
        return null;
    }
}
