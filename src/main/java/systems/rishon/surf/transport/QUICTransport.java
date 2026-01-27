package systems.rishon.surf.transport;

import io.netty.util.AttributeKey;

import java.security.cert.X509Certificate;

public final class QUICTransport {

    public static final AttributeKey<X509Certificate> CLIENT_CERTIFICATE_ATTR =
            AttributeKey.valueOf("CLIENT_CERTIFICATE");

    public static final AttributeKey<Integer> ALPN_REJECT_ERROR_CODE_ATTR =
            AttributeKey.valueOf("ALPN_REJECT_ERROR_CODE");

    private QUICTransport() {
    }
}