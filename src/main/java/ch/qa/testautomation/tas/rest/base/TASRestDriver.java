package ch.qa.testautomation.tas.rest.base;

import ch.qa.testautomation.tas.exception.ExceptionBase;
import ch.qa.testautomation.tas.exception.ExceptionErrorKeys;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.stream.Collectors;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.debug;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;

/**
 * general rest driver with basic authentication
 */
public class TASRestDriver extends SimpleRestDriver {

    protected Response response;
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

    public void setAuthorizationToken(String authorizationToken) {
        addHeader("Authorization", authorizationToken);
    }

    public String getCookies() {
        printCookies();
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining(";"));
    }

    @Override
    public void close() {
        response = null;
        super.close();
    }

    @Override
    public Response get(String path) {
        response = super.get(getQueryUrl(path));
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

    @Override
    public Response post(String path, String payload) {
        response = super.post(getQueryUrl(path), payload);
        return response;
    }

    public Response post(String path, String payload, Map<String, String> params) {
        setParams(params);
        response = post(path, payload);
        return response;
    }

    @Override
    public Response put(String path, String payload) {
        response = super.put(getQueryUrl(path), payload);
        return response;
    }

    @Override
    public Response delete(String path) {
        response = super.delete(getQueryUrl(path));
        return response;
    }

    public Response patch(String path, String payload) {
        response = super.patch(getQueryUrl(path), payload);
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

    public void printCookies() {
        response.getCookies().forEach((key, value) -> debug("Cookie: " + key + "->" + value));
    }

    public void secureParameter(String param, String paramName) {
        if (!isValid(param)) {
            throw new ExceptionBase(ExceptionErrorKeys.NULL_EXCEPTION_EMPTY, paramName);
        }
    }

    public void setHost(String host) {
        this.host = host;
    }

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
