package ch.qa.testautomation.framework.rest.TFS.connection;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.rest.base.RestDriverBase;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

import static ch.qa.testautomation.framework.common.logging.SystemLogger.log;
import static ch.qa.testautomation.framework.common.logging.SystemLogger.trace;

public class TFSConnector extends RestDriverBase {
    private String apiVersion;

    /**
     * construct connector with host, apiVersion and personalToken
     *
     * @param host          host: "https://xxxx.com:port/"
     * @param personalToken personalToken
     * @param apiVersion    api-version of tfs for post, put and update
     */
    public TFSConnector(String host, String personalToken, String apiVersion) {
        super(host);
        this.apiVersion = apiVersion;
        setAuthorizationToken("Basic " + PropertyResolver.encodeBase64(":" + personalToken));
        initialize();
    }

    public void setApiVersion(String version) {
        apiVersion = version;
    }

    /**
     * download item with query params
     *
     * @param path     path of tfvc
     * @param filePath params for get item
     * @return response
     */
    public Response downloadItem(String path, String filePath) {
        return get(path, "path", filePath, MediaType.APPLICATION_OCTET_STREAM_TYPE);
    }


    /**
     * get with path, single parameter and media type
     *
     * @param path      path
     * @param key       param key
     * @param value     param value
     * @param mediaType {@link MediaType}
     * @return response
     */
    public Response get(String path, String key, String value, MediaType mediaType) {
        log("TRACE", "Request Get: " + path + "\nWith Query: " + key + "=" + value);
        MediaType last = getMediaType();
        setMediaType(mediaType);
        response = get(path, key, value);
        setMediaType(last);
        return response;
    }

    /**
     * get with query like "param name":"value" and media type
     *
     * @param path      path
     * @param params    map of params
     * @param mediaType {@link MediaType}
     * @return response
     */
    public Response get(String path, Map<String, String> params, MediaType mediaType) {
        trace("Request Get: " + path);
        MediaType last = getMediaType();
        setMediaType(mediaType);
        response = get(path, params);
        setMediaType(last);
        return response;
    }


    @Override
    public Response post(String path, String payload) {
        trace("Request POST: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .queryParam("api-version", apiVersion)
                .request(mediaType)
                .header("Authorization", authorizationToken)
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
                .header("Authorization", authorizationToken)
                .put(Entity.entity(payload, mediaType));
        return response;
    }

    @Override
    public Response patch(String path, String payload) {
        trace("Request PATCH: " + path);
//        trace("payload: " + payload);
        response = webTarget.path(path)
                .queryParam("api-version", apiVersion)
                .request(mediaType)
                .header("Authorization", authorizationToken)
                .header("X-HTTP-Method-Override", "PATCH")
                .put(Entity.entity(payload, mediaType));
        return response;
    }


}
