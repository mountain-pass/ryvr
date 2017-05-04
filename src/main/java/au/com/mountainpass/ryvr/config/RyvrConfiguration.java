package au.com.mountainpass.ryvr.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RyvrConfiguration implements
        ApplicationListener<EmbeddedServletContainerInitializedEvent> {

    private int port;

    @Value("${au.com.mountainpass.ryvr.ssl.hostname}")
    private String sslHostname;

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
