package systems.rishon.surf.session;

import io.netty.incubator.codec.quic.QuicStreamChannel;
import lombok.Getter;
import lombok.Setter;
import systems.rishon.surf.config.BackendServer;

public final class ProxyPlayerSession {
    @Getter
    private final String sessionId;
    @Getter
    private BackendServer connectedServer;
    @Getter
    private QuicStreamChannel clientStream;
    @Getter
    @Setter
    private QuicStreamChannel backendStream;

    public ProxyPlayerSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public void attachClientStream(QuicStreamChannel stream) {
        this.clientStream = stream;
    }

    public void attachBackendStream(QuicStreamChannel stream) {
        this.backendStream = stream;
    }

    public QuicStreamChannel clientStream() {
        return clientStream;
    }

    public QuicStreamChannel backendStream() {
        return backendStream;
    }

    public void connectTo(BackendServer server) {
        this.connectedServer = server;
    }
}
