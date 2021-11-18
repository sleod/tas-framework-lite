package ch.qa.testautomation.framework.rest.TFS.connection;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.intefaces.RestDriver;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

public class TFSConnector implements RestDriver {

    private WebTarget webTarget;
    private Response response;
    private Client client;
    private final String host;
    private final String basic;
    private final String apiVersion;
    private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

    /**
     * construct connector with host, apiVersion and personalToken
     *
     * @param host          host: "https://tfs.server.ch:8081/"
     * @param personalToken personalToken
     * @param apiVersion    api-version of tfs for post, put and update
     */
    public TFSConnector(String host, String personalToken, String apiVersion) {
        if (personalToken == null || personalToken.isEmpty()) {
            throw new RuntimeException("Personal Access Token must be provided for TFS Connection!");
        }
        this.host = host;
        this.basic = "Basic " + PropertyResolver.encodeBase64(":" + personalToken);
        this.apiVersion = apiVersion;
        initialize();
    }

    /**
     * Constructor with config in map
     *
     * @param tfsConfig map of configs
     */
    public TFSConnector(Map<String, String> tfsConfig) {
        String personalToken = tfsConfig.get("pat");
        if (personalToken == null || personalToken.isEmpty()) {
            throw new RuntimeException("Personal Access Token must be provided for TFS Connection!");
        }
        this.host = tfsConfig.get("host");
        this.basic = "Basic " + PropertyResolver.encodeBase64(":" + personalToken);
        this.apiVersion = tfsConfig.get("apiVersion");
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
        printCookies();
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue()).collect(Collectors.joining(";"));
    }

    public Response downloadItemsInFolderAsZip(String path, String folder) {
        SystemLogger.trace("Request GET: " + path);
        response = webTarget.path(path)
                .queryParam("path", folder)
                .request(MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .header("Authorization", basic)
                .header("Accept", "application/zip")
                .get();
        return response;
    }

    @Override
    public Response get(String path) {
        SystemLogger.trace("Request GET: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", basic)
                .get();
        return response;
    }

    @Override
    public Response get(String path, String query) {
        try {
            SystemLogger.trace("Request Get: " + path + "\nWith Query: " + query);
            response = webTarget.path(path)
                    .queryParam("query", URLEncoder.encode(query, "UTF-8"))
                    .request(mediaType)
                    .header("Authorization ", basic)
                    .get();
        } catch (UnsupportedEncodingException ex) {
            close();
            SystemLogger.error(ex);
        }
        return response;
    }

    /**
     * regular get with single query like "param name":"value"
     *
     * @param path  path
     * @param key   name of parameter
     * @param value value of parameter
     * @return response
     */
    @Override
    public Response get(String path, String key, String value) {
        SystemLogger.log("TRACE", "Request Get: " + path + "\nWith Query: " + key + "=" + value);
        response = webTarget.path(path)
                .queryParam(key, value)
                .request(mediaType)
                .header("Authorization", basic)
                .get();
        return response;
    }

    @Override
    public Response get(String path, Map<String, String> params) {
        SystemLogger.trace("Request Get: " + path);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            webTarget = webTarget.queryParam(key, value);
            SystemLogger.trace("Query: " + key + "=" + value);
        }
        response = webTarget.path(path).request(mediaType)
                .header("Authorization ", basic)
                .get();
        return response;
    }

    @Override
    public Response post(String path, String payload) {
        SystemLogger.trace("Request POST: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .queryParam("api-version", apiVersion)
                .request(mediaType)
                .header("Authorization ", basic)
                .post(Entity.entity(payload, mediaType));
        return response;
    }

    public Response post(String path, String payload, String apiVersion) {
        SystemLogger.trace("Request POST: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .queryParam("api-version", apiVersion)
                .request(mediaType)
                .header("Authorization ", basic)
                .post(Entity.entity(payload, mediaType));
        return response;
    }

    @Override
    public Response put(String path, String payload) {
        SystemLogger.trace("Request PUT: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .queryParam("api-version", apiVersion)
                .request(mediaType)
                .header("Authorization ", basic)
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    @Override
    public Response delete(String path) {
        SystemLogger.trace("Request DELETE: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization ", basic)
                .delete();
        return response;
    }

    public Response patch(String path, String payload) {
        SystemLogger.trace("Request PATCH: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .queryParam("api-version", apiVersion)
                .request(mediaType)
                .header("Authorization ", basic)
                .header("X-HTTP-Method-Override", "PATCH")
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    private void printCookies() {
        response.getCookies().forEach((key, value) -> SystemLogger.trace("Cookie: " + key + "->" + value));
    }
}