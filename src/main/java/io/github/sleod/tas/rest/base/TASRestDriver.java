package io.github.sleod.tas.rest.base;

import io.github.sleod.tas.exception.ExceptionBase;
import io.github.sleod.tas.exception.ExceptionErrorKeys;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.stream.Collectors;

import static io.github.sleod.tas.common.logging.SystemLogger.debug;
import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;

/**
 * general rest driver with basic authentication
 */
public class TASRestDriver extends SimpleRestDriver {

    protected Response response;
    @Getter
    @Setter
    protected String host = "";

    /**
     * Construction with host and user and password
     *
     * @param host     host
     * @param user     user
     * @param password password
     */
    public TASRestDriver(String host, String user, String password) {
        secureParameter(host, "Host of Endpoint");
        secureParameter(user, "User for Basic Authorization");
        secureParameter(password, "Password for Basic Authorization");
        this.host = host;
        setBasicAuth(user, password);
    }

    /**
     * Construction with host without parameters for authorization
     *
     * @param host host
     */
    public TASRestDriver(String host) {
        secureParameter(host, "Host of Endpoint");
        this.host = host;
    }

    /**
     * Construction with host and authorization Token
     *
     * @param host     host
     * @param patToken personal access token
     */
    public TASRestDriver(String host, String patToken) {
        secureParameter(host, "Host of Endpoint");
        secureParameter(patToken, "Personal Access Token");
        this.host = host;
        setBearerToken(patToken);
    }

    /**
     * Construction with proxy setting
     *
     * @param proxyURL proxy url
     * @param port     port
     * @param user     proxy user
     * @param pass     proxy pass
     */
    public TASRestDriver(String proxyURL, int port, String user, String pass) {
        super(proxyURL, port, user, pass);
    }


    /**
     * Construction with rest config defined host and Authorization parameters
     */
    public TASRestDriver() {
        super();
    }

    /**
     * Set Bearer Token for Authorization Header
     *
     * @param authorizationToken authorization token with "Bearer " prefix
     */
    public void setAuthorizationToken(String authorizationToken) {
        addHeader("Authorization", authorizationToken);
    }

    /**
     * Get all cookies as single string
     *
     * @return cookies as single string
     */
    public String getCookies() {
        printCookies();
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining(";"));
    }

    /**
     * Close the driver and release resources
     */
    @Override
    public void close() {
        response = null;
        super.close();
    }

    /**
     * Not a Regular get with Query, only for query with param name "query" : "value"
     *
     * @param path path
     * @return response
     */
    @Override
    public Response get(String path) {
        response = super.get(getQueryUrl(path));
        return response;
    }

    /**
     * Not a Regular get with Query, only for query with param name "query" : "value"
     *
     * @param path  path
     * @param query normal url query string ends with ?key=value&amp;key2=value2
     * @return response
     */
    @Override
    public Response get(String path, String query) {
        response = super.get(getQueryUrl(path), query);
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
        response = super.get(getQueryUrl(path), key, value);
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
        setParams(params);
        response = get(path);
        return response;
    }

    /**
     * Post with payload
     *
     * @param path    path
     * @param payload payload
     * @return response
     */
    @Override
    public Response post(String path, String payload) {
        response = super.post(getQueryUrl(path), payload);
        return response;
    }

    /**
     * Post with payload and query params
     *
     * @param path    path
     * @param payload payload
     * @param params  map of params
     * @return response
     */
    public Response post(String path, String payload, Map<String, String> params) {
        setParams(params);
        response = post(path, payload);
        return response;
    }

    /**
     * Put with payload
     *
     * @param path    path
     * @param payload payload
     * @return response
     */
    @Override
    public Response put(String path, String payload) {
        response = super.put(getQueryUrl(path), payload);
        return response;
    }

    /**
     * Delete with path
     *
     * @param path path
     * @return response
     */
    @Override
    public Response delete(String path) {
        response = super.delete(getQueryUrl(path));
        return response;
    }

    /**
     * Patch with payload
     *
     * @param path    path
     * @param payload payload
     * @return response
     */
    public Response patch(String path, String payload) {
        response = super.patch(getQueryUrl(path), payload);
        return response;
    }

    /**
     * Reset Media Type to JSON
     */
    public void resetMediaType() {
        this.mediaType = MediaType.APPLICATION_JSON_TYPE;
    }

    /**
     * Get Status of last response
     *
     * @return status code
     */
    public int getStatus() {
        return response.getStatus();
    }

    /**
     * Get Response Message of last response
     *
     * @return response message
     */
    public String getResponseMessage() {
        return response.readEntity(String.class);
    }

    /**
     * Print all cookies to debug log
     */
    public void printCookies() {
        response.getCookies().forEach((key, value) -> debug("Cookie: " + key + "->" + value));
    }

    /**
     * Secure that parameter is not null or empty
     *
     * @param param     parameter to check
     * @param paramName name of parameter for exception message
     */
    public void secureParameter(String param, String paramName) {
        if (!isValid(param)) {
            throw new ExceptionBase(ExceptionErrorKeys.NULL_EXCEPTION_EMPTY, paramName);
        }
    }

    /**
     * Build the full URL for the request
     *
     * @param path path
     * @return full url
     */
    public String getQueryUrl(String path) {
        if (!isValid(host)) {
            throw new ExceptionBase(ExceptionErrorKeys.CUSTOM_MESSAGE, "Host for REST Call is invalid! -> " + host);
        } else if (path.startsWith("http")) {
            return path;
        }
        if (!host.endsWith("/") && !path.startsWith("/")) {
            host += "/";
        }
        return host + path;
    }

}
