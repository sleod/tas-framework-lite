package ch.qa.testautomation.tas.rest.base;

import ch.qa.testautomation.tas.configuration.PropertyResolver;
import ch.qa.testautomation.tas.intefaces.DriverProvider;
import lombok.Setter;

/**
 * Provider for Rest Driver
 */
public class RestDriverProvider implements DriverProvider {

    private TASRestDriver restDriver = null;
    @Setter
    private String user = "";
    private String password = "";
    @Setter
    private String host;
    @Setter
    private String patToken = "";

    /**
     * Set password (base64 encoded)
     */
    public void setPassword(String password) {
        this.password = PropertyResolver.decodeBase64(password);
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

    /**
     * get driver instance, initialize if not done yet
     * @return rest driver
     */
    @Override
    @SuppressWarnings("unchecked")
    public TASRestDriver getDriver() {
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
    public TASRestDriver getDriver(String host, String patToken) {
        setHost(host);
        setPatToken(patToken);
        return restDriver;
    }

    /**
     * get driver with host, user and password
     * @param host     host
     * @param user     user
     * @param password pw
     * @return rest driver
     */
    public TASRestDriver getDriver(String host, String user, String password) {
        setHost(host);
        setUser(user);
        setPassword(password);
        return restDriver;
    }

    /**
     * close driver
     */
    @Override
    public void close() {
        if (restDriver != null) {
            restDriver.close();
        }
    }

    /**
     * initialize driver
     */
    @Override
    public void initialize() {
        if (!host.isEmpty()) {
            if (!user.isEmpty() && !password.isEmpty()) {
                restDriver = new TASRestDriver(host, user, password);
            } else if (!patToken.isEmpty()) {
                restDriver = new TASRestDriver(host, patToken);
            } else {
                restDriver = new TASRestDriver(host);
            }
        } else {
            restDriver = new TASRestDriver();
        }
    }

    /**
     * set driver instance
     * @param restDriver rest driver
     */
    public void setDriver(TASRestDriver restDriver) {
        this.restDriver = restDriver;
    }
}
