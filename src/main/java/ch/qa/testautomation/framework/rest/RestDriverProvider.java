package ch.qa.testautomation.framework.rest;

import ch.qa.testautomation.framework.configuration.PropertyResolver;
import ch.qa.testautomation.framework.intefaces.DriverProvider;
import ch.qa.testautomation.framework.rest.base.RestDriverBase;

public class RestDriverProvider implements DriverProvider {

    private RestDriverBase restDriver = null;
    private String user = "";
    private String password = "";
    private String host;
    private String patToken = "";

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = PropertyResolver.decodeBase64(password);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPatToken(String patToken) {
        this.patToken = patToken;
    }

    /**
     * Constructor with user pw and host
     *
     * @param user     user
     * @param password pw
     * @param host     host url
     */
    public RestDriverProvider(String user, String password, String host) {
        this.user = user;
        this.password = PropertyResolver.decodeBase64(password);
        this.host = host;
    }

    /**
     * Construction with encoded basic key
     *
     * @param host     host
     * @param patToken personal access token
     */
    public RestDriverProvider(String host, String patToken) {
        this.host = host;
        this.patToken = patToken;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RestDriverBase getDriver() {
        if (restDriver == null) {
            initialize();
        }
        return restDriver;
    }

    /**
     * Construction with host and authorization Token
     *
     * @param host     host
     * @param patToken personal access token
     * @return rest driver
     */
    public RestDriverBase getDriver(String host, String patToken) {
        setHost(host);
        setPatToken(patToken);
        return restDriver;
    }

    /**
     * get driver with host, user and password
     */
    public RestDriverBase getDriver(String host, String user, String password) {
        setHost(host);
        setUser(user);
        setPassword(password);
        return restDriver;
    }

    @Override
    public void close() {
        if (restDriver != null) {
            restDriver.close();
        }
    }

    @Override
    public void initialize() {
        if (!host.isEmpty()) {
            if (!user.isEmpty() && !password.isEmpty()) {
                restDriver = new RestDriverBase(host, user, password);
            } else if (!patToken.isEmpty()) {
                restDriver = new RestDriverBase(host, patToken);
            } else {
                restDriver = new RestDriverBase(host);
            }
        } else {
            restDriver = new RestDriverBase();
        }
    }

    public void setDriver(RestDriverBase restDriver) {
        this.restDriver = restDriver;
    }
}
