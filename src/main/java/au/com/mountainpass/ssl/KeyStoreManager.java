package au.com.mountainpass.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KeyStoreManager {
    public final Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Value("${server.ssl.key-alias}")
    private String keyAlias;

    @Value("${server.ssl.key-password}")
    private String keyPassword;

    @Value("${server.ssl.key-store}")
    private String keyStoreFileName;

    @Value("${server.ssl.key-store-password}")
    private String keyStorePassword;

    private String keyStoreType = KeyStore.getDefaultType();

    private File keyStoreFile;

    private KeyStore keyStore;

    public KeyStoreManager() {

    }

    private KeyStore loadKeyStore(File keyStoreFile, String keyStoreType,
            String keyStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, FileNotFoundException {
        LOGGER.info("Loading Key Store: {}", keyStoreFile.getAbsolutePath());
        KeyStore rval = KeyStore.getInstance(keyStoreType);
        if (keyStoreFile.exists()) {
            rval.load(new FileInputStream(keyStoreFile),
                    keyStorePassword.toCharArray());
        } else {
            rval.load(null, keyStorePassword.toCharArray());
        }
        return rval;
    }

    @PostConstruct
    public void postConstruct()
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, FileNotFoundException, IOException {
        this.keyStoreFileName = getKeyStoreLocation(keyStoreFileName);

        this.keyStoreFile = new File(keyStoreFileName);

        this.keyStore = loadKeyStore(keyStoreFile, keyStoreType,
                keyStorePassword);
    }

    public void addCertificate(KeyPair keyPair, Certificate cert)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {

        keyStore.setKeyEntry(keyAlias, keyPair.getPrivate(),
                keyPassword.toCharArray(), new Certificate[] { cert });
        // Store away the keystore.
        File parent = keyStoreFile.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream(keyStoreFile);
        keyStore.store(fos, keyStorePassword.toCharArray());
        fos.close();
        throw new RuntimeException(keyStoreFile.getAbsolutePath());
    }

    private String getKeyStoreLocation(String trustStoreFile) {
        if (StringUtils.hasLength(trustStoreFile)) {
            return trustStoreFile;
        }
        final String locationProperty = System
                .getProperty("server.ssl.key-store");
        if (StringUtils.hasLength(locationProperty)) {
            return locationProperty;
        } else {
            return "keystore.jks";
        }
    }

    public Certificate getCertificate() throws KeyStoreException {
        return keyStore.getCertificate(keyAlias);
    }

    public String getKeyAlias() {
        return keyAlias;
    }

}
