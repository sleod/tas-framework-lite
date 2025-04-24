package ch.qa.testautomation.tas.rest.base;

import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.intefaces.RestDriver;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;

import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ch.qa.testautomation.tas.common.logging.SystemLogger.*;
import static ch.qa.testautomation.tas.common.utils.StringTextUtils.isValid;

/**
 * general rest driver with basic authentication
 */
public class SimpleRestDriver implements RestDriver {

    private final Client client;
    protected MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
    private final Map<String, String> headers = new HashMap<>();
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

    public Response get(String url) {
        debug("GET: " + url);
        return getRequester(url).get();
    }

    @Override
    public Response get(String path, String query) {
        return get(path + "?query=" + query);
    }

    @Override
    public Response get(String path, String key, String value) {
        addParam(key, value);
        return get(path);
    }

    @Override
    public Response get(String path, Map<String, String> queries) {
        setParams(queries);
        return get(path);
    }

    @Override
    public Response post(String url, String payload) {
        debug("POST: " + url);
        trace(payload);
        return getRequester(url).post(Entity.entity(payload, mediaType));
    }

    public Response post(String url, String payload, Map<String, String> params) {
        setParams(params);
        return post(url, payload);
    }

    @Override
    public Response put(String url, String payload) {
        debug("PUT: " + url);
        trace(payload);
        return getRequester(url).put(Entity.entity(payload, mediaType));
    }

    public Response patch(String url, String payload) {
        debug("PATCH: " + url + System.lineSeparator() + payload);
        return getRequester(url)
                .header("X-HTTP-Method-Override", "PATCH")
                .put(Entity.entity(payload, mediaType));
    }

    @Override
    public Response delete(String url) {
        debug("DELETE: " + url);
        return getRequester(url).delete();
    }

    @Override
    public void close() {
        client.close();
    }

    public int getStatus(Response response) {
        return response.getStatus();
    }

    public Invocation.Builder getRequester(String url) {
        return buildRequester(buildWebTarget(client.target(url)));
    }

    public String getResponseMessage(Response response) {
        return response.readEntity(String.class);
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void setCookie(String cookie) {
        addHeader("Cookie", cookie);
    }

    public void cleanHeaders() {
        headers.clear();
    }

    public void setParams(Map<String, String> params) {
        this.params.putAll(params);
    }

    public void addParam(String key, String value) {
        params.put(key, value);
    }

    public void cleanParams() {
        info("Query Parameters cleaned up!");
        params.clear();
    }

    public String getCookies(Response response) {
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue())
                .collect(Collectors.joining(";"));
    }

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


