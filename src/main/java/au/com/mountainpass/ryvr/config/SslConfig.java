package au.com.mountainpass.ryvr.config;

import java.security.cert.Certificate;

import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import au.com.mountainpass.KeyStoreManager;

@Configuration
public class SslConfig {

    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    @Value("${server.ssl.key-password}")
    private String keyPassword;

    @Value("${server.ssl.key-store}")
    private String keyStore;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    @Value("${au.com.mountainpass.ryvr.ssl.hostname}")
    private String sslHostname;

    @Value("${au.com.mountainpass.ryvr.ssl.genCert:selfSigned}")
    private String genCert;

    @Bean
    public KeyStoreManager keyStoreManager() throws Exception {
        return new KeyStoreManager();
    }

    @Bean
    public Certificate cert() throws Exception {
        return keyStoreManager().generateSelfSignedCertificate(keyStore,
                keyStorePassword, keyPassword, keyAlias, sslHostname);
    }

    @Bean
    public TomcatEmbeddedServletContainerFactory tomcatFactory()
            throws Exception {
        cert();
        return new TomcatEmbeddedServletContainerFactory() {

            @Override
            protected TomcatEmbeddedServletContainer getTomcatEmbeddedServletContainer(
                    final Tomcat tomcat) {
                return super.getTomcatEmbeddedServletContainer(tomcat);
            }
        };
    }

}
