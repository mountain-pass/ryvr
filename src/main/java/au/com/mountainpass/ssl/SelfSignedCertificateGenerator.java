package au.com.mountainpass.ssl;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V1CertificateGenerator;
import org.springframework.stereotype.Component;

@Component(value = "selfSigned")
public class SelfSignedCertificateGenerator implements CertificateGenerator {

    private SecureRandom secureRandom = new SecureRandom();

    static {
        // adds the Bouncy castle provider to java security
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public Certificate generateCertificate(KeyPair keyPair, String domainName)
            throws CertificateEncodingException, InvalidKeyException,
            IllegalStateException, NoSuchProviderException,
            NoSuchAlgorithmException, SignatureException {
        // see
        // http://www.bouncycastle.org/wiki/display/JA1/X.509+Public+Key+Certificate+and+Certification+Request+Generation

        Date startDate = new Date();
        Date expiryDate = new Date(
                System.currentTimeMillis() + (1000L * 60 * 60 * 24));
        BigInteger serialNumber = BigInteger
                .valueOf(Math.abs(secureRandom.nextInt())); // serial
                                                            // number for
                                                            // certificate

        X509V1CertificateGenerator certGen = new X509V1CertificateGenerator();
        X500Principal dnName = new X500Principal("CN=" + domainName);
        certGen.setSerialNumber(serialNumber);
        certGen.setIssuerDN(dnName);
        certGen.setNotBefore(startDate);
        certGen.setNotAfter(expiryDate);
        certGen.setSubjectDN(dnName); // note: same as issuer
        certGen.setPublicKey(keyPair.getPublic());
        certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
        X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC");

        return cert;
    }

    @Override
    public KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA",
                "BC");
        keyPairGenerator.initialize(2048, new SecureRandom());

        return keyPairGenerator.generateKeyPair();

    }

}
