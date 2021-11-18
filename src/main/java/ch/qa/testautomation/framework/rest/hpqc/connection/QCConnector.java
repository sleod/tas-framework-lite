package ch.qa.testautomation.framework.rest.hpqc.connection;

import ch.qa.testautomation.framework.common.logging.SystemLogger;
import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.intefaces.RestDriver;
import ch.qa.testautomation.framework.core.json.deserialization.JSONContainerFactory;
import net.sf.json.JSONObject;

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


public class QCConnector implements RestDriver {

    private WebTarget webTarget;
    private Response response;
    private String currPath;
    private String cookies;
    private Client client;
    private final String host;
    private MediaType mediaType = MediaType.APPLICATION_XML_TYPE;
    private MediaType attachmentMediaType = MediaType.APPLICATION_OCTET_STREAM_TYPE;
    private final String user;
    private final String password;

    /**
     * create new instance of QCConnector with input settings
     *
     * @param host     qc host
     * @param user     qc user
     * @param password qc password
     */
    public QCConnector(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
        initialize();
    }

    /**
     * create new instance of QCConnector with input settings
     *
     * @param configFilePath config file path
     */
    public QCConnector(String configFilePath) {
        JSONObject config = JSONContainerFactory.getConfig(configFilePath);
        this.host = config.getString("host");
        this.user = config.getString("user");
        this.password = config.getString("password");
        initialize();
    }

    @Override
    public final void initialize() {
        if (webTarget == null) {
            this.client = ClientBuilder.newClient();
            this.connect();
            this.response = basicAuthentication("api/authentication/sign-in", user, password);
            this.cookies = getCookies();
        }
    }

    @Override
    public void connect() {
        this.webTarget = client.target(host);
    }

    public String getUser() {
        return user;
    }

    public Response getResponse() {
        return response;
    }

    public Response getEntityFromQCContainer(String path, String query) {
        if (query != null) {
            return get(path, query);
        } else {
            return get(path);
        }
    }

    public Response getEntityFromQCContainer(String path, String container, String query) {
        currPath = path + container;
        return getEntityFromQCContainer(currPath, query);
    }

    public Response getEntityFromQCContainer(String domain, String project, String container, String query) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/";
        currPath = path;
        return getEntityFromQCContainer(path, container, query);
    }

    public Response getEntityFromQCContainerByID(String domain, String project, String container, String id) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + container + "/" + id;
        currPath = path;
        response = get(path);
        return response;
    }

    public Response getAttachmentsFromQCEntity(String domain, String project, String entityID, int entityType, String attachName) {
        String container = QCConstants.getContainerName(entityType);
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + container + "/" + entityID + "/" + QCConstants.ATTACHMENT_CONTAINER;
        currPath = path;
        if (attachName != null) {
            path += "/" + attachName;
        }
        response = requestAttachment(path);
        return response;
    }

    public Response appendAttachmentsToQCEntity(String domain, String project, String entityID, int entityType, String attachName, byte[] content) {
        String container = QCConstants.getContainerName(entityType);
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + container + "/" + entityID + "/" + QCConstants.ATTACHMENT_CONTAINER;
        currPath = path;
        response = getAttachmentsFromQCEntity(domain, project, entityID, entityType, attachName);
        if (response.getStatus() == 200) {
            response = webTarget.path(path + "/" + attachName)
                    .request()
                    .header("Cookie", cookies)
                    .put(Entity.entity(content, attachmentMediaType));
        } else {
            response = webTarget.path(path)
                    .request()
                    .header("Cookie", cookies)
                    .header("Slug", attachName)
                    .header("override-existing-attachment", "y")
                    .post(Entity.entity(content, attachmentMediaType));
        }
        return response;
    }

    public Response createQCEntity(String domain, String project, int entityType, String xmlContent) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/"
                + QCConstants.getContainerName(entityType);
        currPath = path;
        return createQCEntity(path, xmlContent);
    }

    public Response updateQCEntity(String domain, String project, int entityType, String entityId, String uptodate) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/"
                + (QCConstants.getContainerName(entityType) + "/" + entityId);
        return updateQCEntity(path, uptodate);
    }

    public Response updateQCEntityWithVersion(String domain, String project, int entityType, String entityId, String upToDate) {
        //lock entity
        response = lockAndUnlockQCEntity(domain, project, entityType, entityId, "lock");
        SystemLogger.log("INFO", "Locked: " + entityType + " with id: " + entityId);
        //Check out
        response = checkOutQCEntity(domain, project, entityType, entityId);
        SystemLogger.log("INFO", "Checked out: " + entityType + " with id: " + entityId);
        //update entity
        response = updateQCEntity(domain, project, entityType, entityId, upToDate);
        SystemLogger.log("INFO", "updated: " + entityType + " with id: " + entityId);
        //check in
        response = checkInQCEntity(domain, project, entityType, entityId, response.readEntity(String.class));
        SystemLogger.log("INFO", "Checked in: " + +entityType + " with id: " + entityId);
        //unlock
        response = lockAndUnlockQCEntity(domain, project, entityType, entityId, "unlock");
        SystemLogger.log("INFO", "unlocked: " + +entityType + " with id: " + entityId);

        return response;
    }

    public Response updateQCEntityWithVersion(String domain, String project, QCEntity qce) {
        response = updateQCEntityWithVersion(domain, project, qce.getEntityType(), qce.getFieldValue("id"), qce.getXMLContent());
        return response;
    }

    /**
     * get schema of qc entity
     *
     * @param path       rest path
     * @param isRequired if shows only required fields
     * @return response
     */
    public Response getFieldsOfQCEntity(String path, boolean isRequired) {
        if (isRequired) {
            response = webTarget.path(path)
                    .queryParam("required", "true")
                    .request(mediaType)
                    .header("Cookie", cookies)
                    .get();
        } else {
            response = get(path);
        }
        return response;
    }

    /**
     * @param domain     is QC Domain
     * @param project    is QC Project
     * @param entityType 1.Defects, 2.Tests, 3.test folder, 4.runs, 5.TestSets,
     *                   6. TestSets Folder, 7.instance 8.Requirement, 9.requirement folder
     * @param isRequired if only required Fields
     * @return Field Description in XML Form
     */
    public Response getFieldsOfQCEntity(String domain, String project, int entityType, boolean isRequired) {
        String entityName = QCConstants.getEntityName(entityType);
        String path = "rest/domains/" + domain + "/projects/" + project + "/customization/entities/" + entityName + "/fields";
        currPath = path;
        return getFieldsOfQCEntity(path, isRequired);
    }

    /**
     * lock: GET
     * rest/domains/{domain}/projects/{project}/{EntityType}/{EntityID}/lock
     * <p>
     * unlock: DELETE
     * rest/domains/{domain}/projects/{project}/{EntityType}/{Entity ID}/lock
     *
     * @param domain     domain of qc
     * @param project    project of qc
     * @param entityType entity type
     * @param entityId   entity id
     * @param action     = lock or unlock
     * @return response of request
     */
    private Response lockAndUnlockQCEntity(String domain, String project, int entityType, String entityId, String action) {
        String entityName = QCConstants.getContainerName(entityType);
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + entityName + "/" + entityId + "/lock";
        if (action.equalsIgnoreCase("lock")) {
            response = post(path, "");
        } else if (action.equalsIgnoreCase("unlock")) {
            response = delete(path);
        }
        return response;
    }

    public Response delete(String domain, String project, int entityType, String id) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/";
        path += QCConstants.getContainerName(entityType) + "/" + id;
        return delete(path);
    }

    @Override
    public Response delete(String path) {
        SystemLogger.log("INFO", "DELETE: path-> " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Cookie", cookies)
                .delete();
        return response;
    }

    @Override
    public Response put(String path, String xml) {
        SystemLogger.log("INFO", "PUT: path-> " + path + "\nXML-> " + xml);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Cookie", cookies)
                .put(Entity.entity(xml, mediaType));
        return response;
    }

    /**
     * update QC entity with full content
     *
     * @param domain  domain
     * @param project project
     * @param entity  target entity
     * @return response
     */
    public Response updateQCEntity(String domain, String project, QCEntity entity) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + (QCConstants.getContainerName(entity.getEntityType())
                + "/" + entity.getFieldValue("id"));
        return put(path, entity.getXMLContent());
    }

    /**
     * update QC Entity with particle content
     *
     * @param domain   domain
     * @param project  project
     * @param entity   target entity
     * @param upToDate particle content
     * @return response
     */
    public Response updateQCEntity(String domain, String project, QCEntity entity, String upToDate) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + (QCConstants.getContainerName(entity.getEntityType())
                + "/" + entity.getFieldValue("id"));
        return put(path, upToDate);
    }

    @Override
    public Response get(String path) {
        SystemLogger.log("INFO", "GET: path-> " + path);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Cookie", cookies)
                .get();
        return response;
    }

    @Override
    public Response get(String path, String query) {
        try {
            SystemLogger.log("INFO", "Get: path-> " + path + "\nQuery-> " + query);
            response = webTarget.path(path)
                    .queryParam("query", URLEncoder.encode(query, "UTF-8"))
                    .request(mediaType)
                    .header("Cookie", cookies)
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
                .header("Cookie", cookies)
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
                .header("Cookie", cookies)
                .get();
        return response;
    }

    public Response post(String domain, String project, String request, String xml) {
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + request;
        return post(path, xml);
    }

    @Override
    public Response post(String path, String xml) {
        SystemLogger.log("INFO", "POST: path-> " + path + "\nXML-> " + xml);
        response = webTarget.path(path)
                .request(mediaType)
                .header("Cookie", cookies)
                .post(Entity.entity(xml, mediaType));
        return response;
    }

    /**
     * basic authentication
     *
     * @param path     "api/authentication/sign-in"
     * @param username user name
     * @param passw    password
     * @return response of query
     */
    public Response basicAuthentication(String path, String username, String passw) {
        String encoding = PropertyResolver.encodeBase64(username + ":" + passw);
        return signIn(path, encoding);
    }

    /**
     * print response of rest request
     *
     * @param all if print all
     */
    public String printResponse(boolean all) {
        SystemLogger.log("INFO", "\n============getResponse============");
        SystemLogger.log("INFO", String.valueOf(response.getStatus()));
        String entry = "";
        if (all) {
            entry = response.readEntity(String.class);
            SystemLogger.log("INFO", entry);
        }
        return entry;
    }

    @Override
    public void close() {
        String encoded = PropertyResolver.encodeBase64(user + ":" + password);
        response = signOut("api/authentication/sign-out", encoded);
        printCookies();
        response.close();
        webTarget = null;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public MediaType getAttachmentMediaType() {
        return attachmentMediaType;
    }

    public void setAttachmentMediaType(MediaType attachmentMediaType) {
        this.attachmentMediaType = attachmentMediaType;
    }

    private Response signIn(String path, String encoded) {
        SystemLogger.trace("Sign-in: " + path);
        return webTarget.path(path).request().header("Authorization ", "Basic " + encoded).post(null);
    }

    private void printCookies() {
        response.getCookies().forEach((key, value) -> SystemLogger.trace("Cookie: " + key + "->" + value));
    }

    private Response signOut(String path, String encoded) {
        SystemLogger.info("Sign-out: " + path);
        return webTarget.path(path).request().header("Authorization ", "Basic " + encoded).get();
    }

    private Response requestAttachment(String path) {
        MediaType tempMT = this.mediaType;
        this.mediaType = this.attachmentMediaType;
        response = get(path);
        this.mediaType = tempMT;
        return response;
    }

    @Override
    public String getCookies() {
        printCookies();
        return response.getCookies().values().stream().map(cookie -> cookie.getName() + "=" + cookie.getValue()).collect(Collectors.joining(";"));
    }

    /**
     * @param path REST full Path
     * @return REST Response
     */
    private Response createQCEntity(String path, String xml) {
        return post(path, xml);
    }

    private Response updateQCEntity(String path, String uptodate) {
        return put(path, uptodate);
    }

    /**
     * POST: rest/domains/{domain}/projects/{project} /{Entity Type}/{Entity
     * ID}/versions/check-out
     * <p>
     * POST: rest/domains/{domain}/projects/{project} /{Entity Type}/{Entity
     * ID}/versions/check-in
     *
     * @param domain     domain
     * @param project    project
     * @param entityType type of entity
     * @param entityId   id of entity
     * @return response of query
     */
    private Response checkOutQCEntity(String domain, String project, int entityType, String entityId) {
        String container = QCConstants.getContainerName(entityType);
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + container + "/" + entityId + "/versions/check-out";
        String version = "<VersionParameters><Comment>Update Test Case via Automation</Comment></VersionParameters>";
        return post(path, version);
    }

    /**
     * POST: rest/domains/{domain}/projects/{project} /{Entity Type}/{Entity
     * ID}/versions/check-out
     * <p>
     * POST: rest/domains/{domain}/projects/{project} /{Entity Type}/{Entity
     * ID}/versions/check-in
     *
     * @param domain     domain
     * @param project    project
     * @param entityType type of entity
     * @param entityId   id of entity
     * @param xml        xml content
     * @return respose of rest
     */
    private Response checkInQCEntity(String domain, String project, int entityType, String entityId, String xml) {
        String container = QCConstants.getContainerName(entityType);
        String path = "rest/domains/" + domain + "/projects/" + project + "/" + container + "/" + entityId + "/versions/check-in";
        return post(path, xml);
    }

}