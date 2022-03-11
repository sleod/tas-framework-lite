package ch.qa.testautomation.framework.rest;

import ch.qa.testautomation.framework.intefaces.DriverProvider;

public class RestDriverProvider implements DriverProvider {

    private RestfulDriver restfulDriver = null;
    private String user = "";
    private String password = "";
    private String host;
    private String token = "";

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public RestDriverProvider(String user, String password, String host) {
        this.user = user;
        this.password = password;
        this.host = host;
    }

    /**
     * Construction with encoded basic key
     *
     * @param host  host
     * @param token 'Basic base64.encode(user:password)' or 'Bearer PAT'
     */
    public RestDriverProvider(String host, String token) {
        this.host = host;
        this.token = token;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RestfulDriver getDriver() {
        if (restfulDriver == null) {
            initialize();
        }
        return restfulDriver;
    }

    /**
     * get driver with host and token
     *
     * @param host  host
     * @param token is 'Basic base64.encode(user:password)' or 'Bearer PAT'
     * @return rest driver
     */
    public RestfulDriver getDriver(String host, String token) {
        setHost(host);
        setToken(token);
        initialize();
        return restfulDriver;
    }

    public RestfulDriver getDriver(String host, String user, String password) {
        setHost(host);
        setUser(user);
        setPassword(password);
        initialize();
        return restfulDriver;
    }

    @Override
    public void close() {
        if (restfulDriver != null) {
            restfulDriver.close();
        }
    }

    @Override
    public void initialize() {
        if (!host.isEmpty()) {
            if (!user.isEmpty() && !password.isEmpty()) {
                restfulDriver = new RestfulDriver(host, user, password);
            } else if (!token.isEmpty()) {
                restfulDriver = new RestfulDriver(host, token);
            } else {
                throw new RuntimeException("Neither user/password nor basic key was given! REST Driver Init failed!");
            }
        } else {
            throw new RuntimeException("Host of Rest Driver is empty! REST Driver Init failed!");
        }
        restfulDriver.initialize();
    }

    public void setDriver(RestfulDriver restDriver) {
        this.restfulDriver = restDriver;
    }
}
