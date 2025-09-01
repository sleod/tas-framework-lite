package io.github.sleod.tas.rest.base;

import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.intefaces.RestDriver;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import lombok.Setter;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static io.github.sleod.tas.common.logging.SystemLogger.*;
import static io.github.sleod.tas.common.utils.StringTextUtils.isValid;

/**
 * general rest driver with basic authentication
 */
public class SimpleRestDriver implements RestDriver {

    @Getter
    private final Client client;
    @Getter
    @Setter
    protected MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    @Getter
    private final Map<String, String> headers = new HashMap<>();
    @Getter
    private final Map<String, String> params = new HashMap<>();

    /**
     * Construction without any inputs
     */
    public SimpleRestDriver() {
        client = ClientBuilder.newClient();
    }

    /**
     * Construction without any inputs
     */
    public SimpleRestDriver(MediaType mediaType) {
        client = ClientBuilder.newClient();
        this.mediaType = mediaType;
    }

    /**
     * Constructor with Proxy settings
     *
     * @param proxyURL proxy url
     * @param port     proxy port
     * @param user     proxy user, empty string for no user required
     * @param pass     proxy pass, empty string for no pass required
     */
    public SimpleRestDriver(String proxyURL, int port, String user, String pass) {
        if(isValid(proxyURL) && isValid(port)) {
            HttpUrlConnectorProvider connectorProvider = new HttpUrlConnectorProvider()
                    .connectionFactory(new ProxyConnectionFactory(Proxy.Type.HTTP, proxyURL, port));
            ClientConfig config = new ClientConfig().connectorProvider(connectorProvider);
            if (isValid(user) && isValid(pass)) {
                config.property(ClientProperties.PROXY_USERNAME, user).property(ClientProperties.PROXY_PASSWORD, pass);
            }
            client = ClientBuilder.newClient(config);
        }else {
            client = ClientBuilder.newClient();
        }
    }

    /**
     * Set Basic Authentication
     *
     * @param user     user
     * @param password pass plain text
     */
    public void setBasicAuth(String user, String password) {
        String authorizationToken = "Basic " + PropertyResolver.encodeBase64(user + ":" + password);
        addHeader("Authorization", authorizationToken);
    }

    /**
     * Set Bearer Token
     *
     * @param authorizationToken Bearer Token
     */
    public void setBearerToken(String authorizationToken) {
        addHeader("Authorization", "Bearer " + authorizationToken);
    }

    /**
     * Get Request
     * @param url URL for GET request
     * @return Response from GET request
     */
    @Override
    public Response get(String url) {
        debug("GET: " + url);
        return getRequester(url).get();
    }

    /**
     * Get Request with single Query Parameter "query"
     * @param path URL for GET request
     * @param query Parameter Value for "query"
     * @return Response from GET request
     */
    @Override
    public Response get(String path, String query) {
        return get(path + "?query=" + query);
    }

    /**
     * Get Request with single Query Parameter
     * @param path URL for GET request
     * @param key Parameter Key
     * @param value Parameter Value
     * @return Response from GET request
     */
    @Override
    public Response get(String path, String key, String value) {
        addParam(key, value);
        return get(path);
    }

    /**
     * Get Request with Query Parameters
     * @param path URL for GET request
     * @param queries Map of Query Parameters
     * @return Response from GET request
     */
    @Override
    public Response get(String path, Map<String, String> queries) {
        setParams(queries);
        return get(path);
    }

    /**
     * Post Request
     * @param url URL for POST request
     * @param payload Payload for POST request
     * @return Response from POST request
     */
    @Override
    public Response post(String url, String payload) {
        debug("POST: " + url);
        trace(payload);
        return getRequester(url).post(Entity.entity(payload, mediaType));
    }

    /**
     * Post Request with Query Parameters
     * @param url URL for POST request
     * @param payload Payload for POST request
     * @param params Map of Query Parameters
     * @return Response from POST request
     */
    public Response post(String url, String payload, Map<String, String> params) {
        setParams(params);
        return post(url, payload);
    }

    /**
     * Put Request
     * @param url URL for PUT request
     * @param payload Payload for PUT request
     * @return Response from PUT request
     */
    @Override
    public Response put(String url, String payload) {
        debug("PUT: " + url);
        trace(payload);
        return getRequester(url).put(Entity.entity(payload, mediaType));
    }

    /**
     * Patch Request
     * @param url URL for PATCH request
     * @param payload Payload for PATCH request
     * @return Response from PATCH request
     */
    public Response patch(String url, String payload) {
        debug("PATCH: " + url + System.lineSeparator() + payload);
        return getRequester(url)
                .header("X-HTTP-Method-Override", "PATCH")
                .put(Entity.entity(payload, mediaType));
    }

    /**
     * Delete Request
     * @param url URL for DELETE request
     * @return Response from DELETE request
     */
    @Override
    public Response delete(String url) {
        debug("DELETE: " + url);
        return getRequester(url).delete();
    }

    /**
     * Close Client
     */
    @Override
    public void close() {
        client.close();
    }

    /**
     * Get Status Code from Response
     * @param response Response from REST call
     * @return Status Code from Response
     */
    public int getStatus(Response response) {
        return response.getStatus();
    }

    /**
     * Get Invocation.Builder for REST call
     * @param url URL for REST call
     * @return Invocation.Builder for REST call
     */
    public Invocation.Builder getRequester(String url) {
        return buildRequester(buildWebTarget(client.target(url)));
    }

    /**
     * Get Response Message as String
     * @param response Response from REST call
     * @return Response Message as String
     */
    public String getResponseMessage(Response response) {
        return response.readEntity(String.class);
    }

    /**
     * Set Headers
     * @param headers Map of Headers
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    /**
     * Add single Header
     * @param key Header Key
     * @param value Header Value
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Set Cookie Header
     * @param cookie Cookie as String
     */
    public void setCookie(String cookie) {
        addHeader("Cookie", cookie);
    }

    /**
     * Clean Headers
     */
    public void cleanHeaders() {
        headers.clear();
    }

    /**
     * Set Query Parameters
     * @param params Map of Query Parameters
     */
    public void setParams(Map<String, String> params) {
        this.params.putAll(params);
    }

    /**
     * Add single Query Parameter
     * @param key Parameter Key
     * @param value Parameter Value
     */
    public void addParam(String key, String value) {
        params.put(key, value);
    }

    /**
     * Clean Query Parameters
     */
    public void cleanParams() {
        info("Query Parameters cleaned up!");
        params.clear();
    }

    /**
     * Get Cookies from Response as String
     * @param response Response from REST call
     * @return Cookies as String
     */
    public String getCookies(Response response) {
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining(";"));
    }

    /**
     * Build Invocation.Builder with Headers
     * @param webTarget WebTarget with Query Parameters
     * @return Invocation.Builder with Headers
     */
    protected Invocation.Builder buildRequester(WebTarget webTarget) {
        Invocation.Builder invocationBuilder = webTarget.request(mediaType);
        if (!headers.isEmpty()) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                invocationBuilder = invocationBuilder.header(key, value);
                debug("Set Header: " + key + "=" + value);
            }
        } else {
            debug("Map of Header is empty!");
        }
        return invocationBuilder;
    }

    /**
     * Build WebTarget with Query Parameters
     * @param webTarget initial WebTarget
     * @return WebTarget with Query Parameters
     */
    protected WebTarget buildWebTarget(WebTarget webTarget) {
        if (!params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                webTarget = webTarget.queryParam(key, value);
                debug("Set Query Param: " + key + "=" + value);
            }
        } else {
            debug("Map of Parameter is empty!");
        }
        cleanParams();
        return webTarget;
    }

}


