package io.github.sleod.tas.rest.base;

import org.glassfish.jersey.client.HttpUrlConnectorProvider.ConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

/**
 * A ConnectionFactory that creates HttpURLConnection instances using a specified proxy.
 */
public class ProxyConnectionFactory implements ConnectionFactory {
    private final Proxy proxy;

    /**
     * Constructs a ProxyConnectionFactory with the specified proxy type, hostname, and port.
     *
     * @param type     the type of the proxy (e.g., Proxy.Type.HTTP, Proxy.Type.SOCKS)
     * @param hostname the hostname of the proxy server
     * @param port     the port number of the proxy server
     */
    public ProxyConnectionFactory(Proxy.Type type, String hostname, int port) {
        proxy = new Proxy(type, new InetSocketAddress(hostname, port));
    }

    /**
     * Opens a connection to the specified URL using the configured proxy.
     *
     * @param url the URL to connect to
     * @return an HttpURLConnection instance connected through the proxy
     * @throws IOException if an I/O error occurs while opening the connection
     */
    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection(proxy);
    }
}
