package ch.qa.testautomation.framework.rest.base;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.exception.ApollonBaseException;
import ch.qa.testautomation.framework.exception.ApollonErrorKeys;
import ch.qa.testautomation.framework.intefaces.RestDriver;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.stream.Collectors;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.warn;
import static ch.qa.testautomation.framework.common.utils.StringTextUtils.isValid;

/**
 * general rest driver with basic authentication
 */
public class RestDriverBase implements RestDriver {
    protected WebTarget webTarget;
    protected Response response;
    protected Client client;
    protected MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    protected String host;
    protected String authorizationToken = "";

    /**
     * Construction with host and user and password
     *
     * @param host     host
     * @param user     user
     * @param password password
     */
    public RestDriverBase(String host, String user, String password) {
        secureParameter(host, "Host of Endpoint");
        secureParameter(user, "User for Basic Authorization");
        secureParameter(password, "Password for Basic Authorization");
        this.host = host;
        this.authorizationToken = "Basic " + PropertyResolver.encodeBase64(user + ":" + password);
        initialize();
    }

    /**
     * Construction with host without parameters for authorization
     *
     * @param host host
     */
    public RestDriverBase(String host) {
        secureParameter(host, "Host of Endpoint");
        this.host = host;
        this.authorizationToken = "";
        initialize();
    }

    /**
     * Construction with host and authorization Token
     *
     * @param host     host
     * @param patToken personal access token
     */
    public RestDriverBase(String host, String patToken) {
        secureParameter(host, "Host of Endpoint");
        secureParameter(patToken, "Personal Access Token");
        this.host = host;
        this.authorizationToken = "Bearer " + patToken;
        initialize();
    }

    /**
     * Construction with rest config defined host and Authorization parameters
     */
    public RestDriverBase() {
        host = PropertyResolver.getRestHost();
        if (isValid(host)) {
            if (isValid(PropertyResolver.getRestUser()) && isValid(PropertyResolver.getRestPassword())) {
                authorizationToken = "Basic " + PropertyResolver.encodeBase64(PropertyResolver.getRestUser() + ":" + PropertyResolver.getRestPassword());
            } else {
                secureParameter(PropertyResolver.getRestPAT(), "Personal Access Token");
                authorizationToken = "Bearer " + PropertyResolver.getRestPAT();
            }
            initialize();
        }else {
            warn("Host is not set! Rest Driver is not initialized! Please setHost() first then initialize()!");
        }
    }

    public void setAuthorizationToken(String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    @Override
    public void connect() {
        this.webTarget = client.target(host);
    }

    public void reconnect(String host) {
        setHost(host);
        connect();
    }

    @Override
    public void initialize() {
        if (authorizationToken.isEmpty()) {
            warn("No Authorization Parameter for Rest Driver Set!");
        }
        if (webTarget == null) {
            this.client = ClientBuilder.newClient();
            this.connect();
        }
    }

    @Override
    public String getCookies() {
        printCookies();
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining(";"));
    }

    @Override
    public void close() {
        webTarget = null;
        response = null;
        client.close();
    }

    @Override
    public Response get(String path) {
        trace("Request GET: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authorizationToken)
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
        trace("Request Get: " + path + "\nWith Query: " + query);
        response = webTarget.path(path)
                .queryParam("query", RestClientBase.encodeUrlPath(query))
                .request(mediaType)
                .header("Authorization", authorizationToken)
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
        trace("Request Get: " + path + "\nWith Query: " + key + "=" + value);
        response = webTarget.path(path)
                .queryParam(key, value)
                .request(mediaType)
                .header("Authorization", authorizationToken)
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
                .header("Authorization", authorizationToken)
                .get();
        connect();//reset to host
        return response;
    }

    @Override
    public Response post(String path, String payload) {
        trace("Request POST: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authorizationToken)
                .post(Entity.entity(payload, mediaType));
        return response;
    }

    public Response post(String path, String payload, Map<String, String> params) {
        trace("Request POST: " + path);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            webTarget = webTarget.queryParam(key, value);
            trace("Query: " + key + "=" + value);
        }
        response = webTarget.path(path).request(mediaType)
                .header("Authorization", authorizationToken)
                .post(Entity.entity(payload, mediaType));
        connect();
        return response;
    }

    @Override
    public Response put(String path, String payload) {
        trace("Request POST: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authorizationToken)
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    @Override
    public Response delete(String path) {
        trace("Request DELETE: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authorizationToken)
                .delete();
        return response;
    }

    public Response patch(String path, String payload) {
        trace("Request PATCH: " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Authorization", authorizationToken)
                .header("X-HTTP-Method-Override", "PATCH")
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Reset Media Type to JSON
     */
    public void resetMediaType() {
        this.mediaType = MediaType.APPLICATION_JSON_TYPE;
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

    public void printCookies() {
        response.getCookies().forEach((key, value) -> trace("Cookie: " + key + "->" + value));
    }

    public void secureParameter(String param, String paramName) {
        if (!isValid(param)) {
            throw new ApollonBaseException(ApollonErrorKeys.NULL_EMPTY_EXCEPTION, paramName);
        }
    }

    public void setHost(String host) {
        this.host = host;
    }
}
