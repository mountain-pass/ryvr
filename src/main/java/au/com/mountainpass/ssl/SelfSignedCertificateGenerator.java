package au.com.mountainpass.ssl;

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

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
      throws CertificateEncodingException, InvalidKeyException, IllegalStateException,
      NoSuchProviderException, NoSuchAlgorithmException, SignatureException {
    // see
    // http://www.bouncycastle.org/wiki/display/JA1/X.509+Public+Key+Certificate+and+Certification+Request+Generation

    return org.keycloak.common.util.CertificateUtils.generateV1SelfSignedCertificate(keyPair,
        domainName);

    // Date startDate = new Date();
    // Date expiryDate = new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24));
    // BigInteger serialNumber = BigInteger.valueOf(Math.abs(secureRandom.nextInt())); // serial
    // // number for
    // // certificate
    //
    // X500Name dnName = new X500Name("CN=" + domainName);
    // Date validityStartDate = new Date(System.currentTimeMillis() - 100000);
    // SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo
    // .getInstance(keyPair.getPublic().getEncoded());
    //
    // X509v1CertificateBuilder certGen = new X509v1CertificateBuilder(dnName, serialNumber,
    // startDate,
    // expiryDate, dnName, subPubKeyInfo);
    //
    // X509v1CertificateBuilder certGen = new X509v1CertificateBuilder();
    // certGen.setSerialNumber(serialNumber);
    // certGen.setIssuerDN(dnName);
    // certGen.setNotBefore(startDate);
    // certGen.setNotAfter(expiryDate);
    // certGen.setSubjectDN(dnName); // note: same as issuer
    // certGen.setPublicKey(keyPair.getPublic());
    // certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");
    // X509Certificate cert = certGen.build(createSigner(keyPair.getPrivate()));
    //
    // return cert;
  }

  @Override
  public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
    keyPairGenerator.initialize(2048, new SecureRandom());

    return keyPairGenerator.generateKeyPair();

  }

}
