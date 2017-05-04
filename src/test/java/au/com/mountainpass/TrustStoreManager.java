package au.com.mountainpass;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import org.springframework.core.io.FileSystemResource;
import org.springframework.util.StringUtils;

public class TrustStoreManager {

    private File trustStoreFile;
    private String trustStorePassword;
    private KeyStore keyStore;
    private String trustStoreLocation;

    public TrustStoreManager(String trustStoreFileName, String trustStoreType,
            String trustStorePassword) throws KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        this.trustStoreLocation = getTrustStoreLocation(trustStoreFileName);

        this.trustStoreFile = new File(
                getTrustStoreLocation(this.trustStoreLocation));

        this.trustStorePassword = trustStorePassword;

        this.keyStore = loadKeyStore(trustStoreType, trustStorePassword);
    }

    private KeyStore loadKeyStore(String trustStoreType,
            String trustStorePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, FileNotFoundException {
        KeyStore keyStore = KeyStore.getInstance(trustStoreType);
        if (trustStoreFile.exists()) {
            keyStore.load(new FileInputStream(trustStoreFile),
                    trustStorePassword.toCharArray());
        } else {
            keyStore.load(null, trustStorePassword.toCharArray());
        }
        return keyStore;
    }

    public void addCert(String keyAlias, Certificate cert)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        keyStore.setCertificateEntry(keyAlias, cert);
        trustStoreFile.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(trustStoreFile);
        keyStore.store(fos, trustStorePassword.toCharArray());
        fos.close();
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    private String getTrustStoreLocation(String trustStoreFile) {
        if (StringUtils.hasLength(trustStoreFile)) {
            return trustStoreFile;
        }
        final String locationProperty = System
                .getProperty("javax.net.ssl.trustStore");
        if (StringUtils.hasLength(locationProperty)) {
            return locationProperty;
        } else {
            return systemDefaultTrustStoreLocation();
        }
    }

    public String systemDefaultTrustStoreLocation() {
        final String javaHome = System.getProperty("java.home");
        final FileSystemResource location = new FileSystemResource(
                javaHome + "/lib/security/jssecacerts");
        if (location.exists()) {
            return location.getFilename();
        } else {
            return javaHome + "/lib/security/cacerts";
        }
    }

    public boolean isSystemDefaultTrustStore() {
        return this.trustStoreLocation
                .equals(systemDefaultTrustStoreLocation());
    }

    public String getTrustStoreLocation() {
        return trustStoreLocation;
    }

}
