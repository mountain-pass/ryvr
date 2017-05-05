package au.com.mountainpass.ryvr.config;

import java.security.cert.CertificateException;

public class CertificateNotFoundException extends CertificateException {

    private static final long serialVersionUID = 1261886649066122734L;

    public CertificateNotFoundException(String keyAlias) {
        super("Certificate with keyAlias '" + keyAlias
                + "' not found and au.com.mountainpass.ryvr.ssl.genCert is 'false'.");
    }

}
