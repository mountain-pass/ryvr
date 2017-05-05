package au.com.mountainpass.ssl;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;

public interface CertificateGenerator {

    KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException;

    Certificate generateCertificate(KeyPair keyPair, String domainName)
            throws CertificateEncodingException, InvalidKeyException,
            IllegalStateException, NoSuchProviderException,
            NoSuchAlgorithmException, SignatureException;

}
