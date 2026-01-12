package systems.rishon.surf.bootstrap;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

public class DynamicCert {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static KeyPairAndCert generate(String commonName) throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        long now = System.currentTimeMillis();
        Date notBefore = new Date(now);
        Date notAfter = new Date(now + 365L * 24 * 60 * 60 * 1000);

        X500Name dnName = new X500Name("CN=" + commonName);
        BigInteger serial = BigInteger.valueOf(now);

        X509v3CertificateBuilder certBuilder =
                new JcaX509v3CertificateBuilder(
                        dnName,
                        serial,
                        notBefore,
                        notAfter,
                        dnName,
                        keyPair.getPublic()
                );

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
        X509Certificate cert = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certBuilder.build(signer));

        return new KeyPairAndCert(keyPair.getPrivate(), cert);
    }

    public record KeyPairAndCert(PrivateKey key, X509Certificate cert) {
    }
}
