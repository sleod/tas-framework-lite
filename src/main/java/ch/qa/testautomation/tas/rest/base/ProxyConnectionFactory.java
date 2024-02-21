package ch.qa.testautomation.tas.rest.base;

import org.glassfish.jersey.client.HttpUrlConnectorProvider.ConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyConnectionFactory implements ConnectionFactory {
    private final Proxy proxy;

    public ProxyConnectionFactory(Proxy.Type type, String hostname, int port) {
        proxy = new Proxy(type, new InetSocketAddress(hostname, port));
    }

    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection(proxy);
    }
}
