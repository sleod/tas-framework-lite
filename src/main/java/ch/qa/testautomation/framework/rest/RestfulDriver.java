package ch.qa.testautomation.framework.rest;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.intefaces.RestDriver;
import ch.qa.testautomation.framework.rest.base.RestClientBase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.log;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;

/**
 * general rest driver with basic authentication
 */
public class RestfulDriver implements RestDriver {
    private WebTarget webTarget;
    private Response response;
    private Client client;
    private final String host;
    private MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    private final String token;

    public RestfulDriver(String host, String user, String password) {
        this.host = host;
        this.token = "Basic " + PropertyResolver.encodeBase64(user + ":" + password);
    }

    /**
     * Construction with token or encoded key
     *
     * @param host  host
     * @param token 'Basic base64.encode(user:password)' or 'Bearer PAT'
     */
    public RestfulDriver(String host, String token) {
        this.host = host;
        this.token = token;
    }

    @Override
    public void connect() {
        this.webTarget = client.target(host);
    }

    @Override
    public void initialize() {
        if (webTarget == null) {
            this.client = ClientBuilder.newClient();
            this.connect();
        }
    }

    @Override
    public String getCookies() {
        printCookies();
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue()).collect(Collectors.joining(";"));
    }

    @Override
    public void close() {
        webTarget = null;
        client.close();
    }

    @Override
    public Response get(String path) {
        log("INFO", "Request GET: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", token)
                .get();
        return response;
    }

    /**
     * Not a Regular get with Query, only for query with param name "query" : "value"
     *
     * @param path  path
     * @param query query like "?sss=xxx&aaa=bbb"
     * @return response
     */
    @Override
    public Response get(String path, String query) {
        log("INFO", "Request Get: " + path + "\nWith Query: " + query);
        response = webTarget.path(path)
                .queryParam("query", RestClientBase.encodeUrlPath(query))
                .request(mediaType)
                .header("Authorization", token)
                .get();
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
                .header("Authorization", token)
                .get();
        return response;
    }

    /**
     * regular get with query like "param name":"value"
     *
     * @param path   path
     * @param params map of params
     * @return response
     */
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
                .header("Authorization", token)
                .get();
        connect();//reset to host
        return response;
    }

    @Override
    public Response post(String path, String payload) {
        log("INFO", "Request POST: " + path + "\nPayload: " + payload);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", token)
                .post(Entity.entity(payload, mediaType));
        return response;
    }

    @Override
    public Response put(String path, String payload) {
        log("INFO", "Request POST: " + path + "\nPayload: " + payload);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", token)
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    @Override
    public Response delete(String path) {
        log("INFO", "Request DELETE: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", token)
                .delete();
        return response;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public int getStatus() {
        return response.getStatus();
    }

    public String getResponseMessage() {
        return response.readEntity(String.class);
    }

    public WebTarget getRequester() {
        return webTarget;
    }

    private void printCookies() {
        response.getCookies().forEach((key, value) -> SystemLogger.trace("Cookie: " + key + "->" + value));
    }
}
