package io.github.sleod.tas.rest.base;

import lombok.Setter;
import org.glassfish.jersey.client.HttpUrlConnectorProvider.ConnectionFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * A ConnectionFactory that creates HttpURLConnection instances using a specified proxy.
 */
public class ProxyConnectionFactory implements ConnectionFactory {
    private final Proxy.Type proxyType;
    private final String proxyHost;
    private final int proxyPort;

    @Setter
    private List<String> excludedHosts = Collections.emptyList();

    public ProxyConnectionFactory(Proxy.Type proxyType, String proxyHost, int proxyPort) {
        this.proxyType = proxyType;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        if (shouldUseProxy(url.getHost())) {
            Proxy proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
            return (HttpURLConnection) url.openConnection(proxy);
        } else {
            return (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
        }
    }

    private boolean shouldUseProxy(String host) {
        if (host == null) return false;
        if (host.startsWith("192.168.") || host.startsWith("10.") || host.endsWith(".local") || host.equalsIgnoreCase("localhost")) {
            return false;
        }
        return excludedHosts.stream().noneMatch(excluded -> host.equalsIgnoreCase(excluded) || host.endsWith("." + excluded)
        );
    }

}