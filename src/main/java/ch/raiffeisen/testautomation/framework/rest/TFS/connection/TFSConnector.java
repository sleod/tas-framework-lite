package ch.raiffeisen.testautomation.framework.rest.TFS.connection;

import ch.raiffeisen.testautomation.framework.configuration.PropertyResolver;
import ch.raiffeisen.testautomation.framework.intefaces.RestDriver;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.raiffeisen.testautomation.framework.common.logging.SystemLogger.*;

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
     * @param host          host: "https://tfs-prod.service.raiffeisen.ch:8081/"
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
        trace("Request GET: " + path);
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
        trace("Request GET: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", basic)
                .get();
        return response;
    }

    @Override
    public Response get(String path, String query) {
        try {
            trace("Request Get: " + path + "\nWith Query: " + query);
            response = webTarget.path(path)
                    .queryParam("query", URLEncoder.encode(query, "UTF-8"))
                    .request(mediaType)
                    .header("Authorization ", basic)
                    .get();
        } catch (UnsupportedEncodingException ex) {
            close();
            error(ex);
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
        log("TRACE", "Request Get: " + path + "\nWith Query: " + key + "=" + value);
        response = webTarget.path(path)
                .queryParam(key, value)
                .request(mediaType)
                .header("Authorization", basic)
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
        response = webTarget.path(path).request(mediaType)
                .header("Authorization ", basic)
                .get();
        return response;
    }

    @Override
    public Response post(String path, String payload) {
        trace("Request POST: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .queryParam("api-version", apiVersion)
                .request(mediaType)
                .header("Authorization ", basic)
                .post(Entity.entity(payload, mediaType));
        return response;
    }

    public Response post(String path, String payload, String apiVersion) {
        trace("Request POST: " + path);
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
        trace("Request PUT: " + path);
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
        trace("Request DELETE: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization ", basic)
                .delete();
        return response;
    }

    public Response patch(String path, String payload) {
        trace("Request PATCH: " + path);
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
        response.getCookies().forEach((key, value) -> trace("Cookie: " + key + "->" + value));
    }
}
