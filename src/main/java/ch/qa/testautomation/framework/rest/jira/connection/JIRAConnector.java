package ch.qa.testautomation.framework.rest.jira.connection;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.intefaces.RestDriver;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.log;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;

public class JIRAConnector implements RestDriver {

    private WebTarget webTarget;
    private Response response;
    private Client client;
    private final String host;
    private final String authHeader;
    private final MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

    /**
     * construct connector with host and personalToken
     *
     * @param host     host: "https://tfs-prod.service.raiffeisen.ch:8081/"
     * @param patToken personalToken
     */
    public JIRAConnector(String host, String patToken) {
        secureParameter(host, "Host of Endpoint");
        secureParameter(patToken, "Personal Access Token");
        this.host = host;
        this.authHeader = "Bearer " + patToken;
        initialize();
    }

    /**
     * construct connector with basic authorization
     *
     * @param host     host: "https://tfs-prod.service.raiffeisen.ch:8081/"
     * @param user     user for Basic Authorization
     * @param password password for Basic Authorization
     */
    public JIRAConnector(String host, String user, String password) {
        secureParameter(host, "Host of Endpoint");
        secureParameter(user, "User for Basic Authorization");
        secureParameter(password, "Password for Basic Authorization");
        this.host = host;
        this.authHeader = "Basic " + PropertyResolver.encodeBase64(user + ":" + password);
        initialize();
    }

    /**
     * Constructor with config in map
     *
     * @param jiraConfig map of configs
     */
    public JIRAConnector(Map<String, String> jiraConfig) {
        String patToken = jiraConfig.get("pat");
        String user = jiraConfig.get("user");
        String password = jiraConfig.get("password");
        String host = jiraConfig.get("host");
        secureParameter(host, "Host of Endpoint");
        this.host = host;
        if (patToken != null && !patToken.isEmpty()) {
            this.authHeader = "Bearer " + patToken;
        } else if (user != null && !user.isEmpty() && password != null && !password.isEmpty()) {
            this.authHeader = "Basic " + PropertyResolver.encodeBase64(user + ":" + password);
        } else {
            throw new RuntimeException("Personal Access Token or User and Password must be provided for Rest Connection!");
        }
        initialize();
    }

    private void secureParameter(String param, String paramName) {
        if (param == null || param.isEmpty()) {
            throw new RuntimeException(paramName + " must not be null or empty!");
        }
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
        response.close();
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


    @Override
    public Response get(String path) {
        trace("Request GET: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authHeader)
                .get();
        return response;
    }

    @Override
    public Response get(String path, String query) {
        trace("Request Get: " + path + "\nWith Query: " + query);
        response = webTarget.path(path)
                .queryParam("query", URLEncoder.encode(query, StandardCharsets.UTF_8))
                .request(mediaType)
                .header("Authorization", authHeader)
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
                .header("Authorization", authHeader)
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
                .header("Authorization", authHeader)
                .get();
        connect();//reset to path
        return response;
    }

    @Override
    public Response post(String path, String payload) {
        trace("Request POST: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authHeader)
                .post(Entity.entity(payload, mediaType));
        return response;
    }


    @Override
    public Response put(String path, String payload) {
        trace("Request PUT: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authHeader)
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    @Override
    public Response delete(String path) {
        trace("Request DELETE: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authHeader)
                .delete();
        return response;
    }

    public Response patch(String path, String payload) {
        trace("Request PATCH: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authHeader)
                .header("X-HTTP-Method-Override", "PATCH")
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    private void printCookies() {
        response.getCookies().forEach((key, value) -> trace("Cookie: " + key + "->" + value));
    }
}
