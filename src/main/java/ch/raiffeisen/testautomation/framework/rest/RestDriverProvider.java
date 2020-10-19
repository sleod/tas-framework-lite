package ch.raiffeisen.testautomation.framework.rest;

import ch.raiffeisen.testautomation.framework.intefaces.DriverProvider;

public class RestDriverProvider implements DriverProvider {

    private RestfulDriver restfulDriver = null;
    private String user = "";
    private String password = "";
    private String host = "";
    private String basic = "";

    public void setUser(String user) {
        this.user = user;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setBasic(String basic) {
        this.basic = basic;
    }

    public RestDriverProvider(String user, String password, String host) {
        this.user = user;
        this.password = password;
        this.host = host;
    }

    /**
     * Construction with encoded basic key
     *
     * @param host       host
     * @param encodedKey base64 encoded key of user:password
     */
    public RestDriverProvider(String host, String encodedKey) {
        this.host = host;
        this.basic = "Basic " + encodedKey;
    }

    @Override
    @SuppressWarnings("unchecked")
    public RestfulDriver getDriver() {
        if (restfulDriver == null) {
            initialize();
        }
        return restfulDriver;
    }

    public RestfulDriver getDriver(String host, String encodedKey) {
        setHost(host);
        setBasic(encodedKey);
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
            } else if (!basic.isEmpty()) {
                restfulDriver = new RestfulDriver(host, basic);
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
