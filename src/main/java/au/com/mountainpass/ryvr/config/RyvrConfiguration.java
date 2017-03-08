package au.com.mountainpass.ryvr.config;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RyvrConfiguration implements
        ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    private int port;

    @Value("${server.ssl.key-store}")
    String keyStore;

    @Value("${server.ssl.key-store-password}")
    String keyStorePassword;

    @Value("${server.ssl.key-password}")
    String keyPassword;

    @Value("${server.ssl.key-alias}")
    String keyAlias;

    @Value("${au.com.windyroad.service-gateway.ssl.hostname}")
    String sslHostname;

    @Value("${javax.net.ssl.trustStore:build/truststore.jks}")
    private String trustStoreFile;

    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public URI getBaseUri() {
        return URI.create("https://" + sslHostname + ":" + getPort());
    }

    @Override
    public void onApplicationEvent(
            EmbeddedServletContainerInitializedEvent event) {
        this.port = event.getEmbeddedServletContainer().getPort();
    }
}
