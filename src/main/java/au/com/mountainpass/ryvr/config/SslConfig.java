package au.com.mountainpass.ryvr.config;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import au.com.mountainpass.ssl.CertificateGenerator;
import au.com.mountainpass.ssl.KeyStoreManager;

@Configuration
public class SslConfig {

    @Value("${au.com.mountainpass.ryvr.ssl.hostname}")
    private String sslHostname;

    @Value("${au.com.mountainpass.ryvr.ssl.genCert:selfSigned}")
    private String genCert;

    @Autowired
    private KeyStoreManager keyStoreManager;

    @Autowired
    AutowireCapableBeanFactory beanFactory;

    @Bean
    public Certificate cert()
            throws CertificateException, NoSuchAlgorithmException,
            NoSuchProviderException, InvalidKeyException, IllegalStateException,
            SignatureException, KeyStoreException, IOException {
        Certificate cert = keyStoreManager.getCertificate();
        if (cert == null) {
            if ("false".equals(genCert)) {
                throw new CertificateNotFoundException(
                        keyStoreManager.getKeyAlias());
            }
            CertificateGenerator certificateGenerator = beanFactory
                    .getBean(genCert, CertificateGenerator.class);
            KeyPair keyPair = certificateGenerator.generateKeyPair();
            cert = certificateGenerator.generateCertificate(keyPair,
                    sslHostname);

            keyStoreManager.addCertificate(keyPair, cert);
        }
        return cert;
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
