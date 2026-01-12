package systems.rishon.surf.session;

import io.netty.incubator.codec.quic.QuicChannel;
import systems.rishon.surf.config.BackendServer;

public final class ProxyPlayerSession {

    private final String sessionId;
    private BackendServer connectedServer;
    private QuicChannel clientChannel;

    public ProxyPlayerSession(String sessionId) {
        this.sessionId = sessionId;
    }

    public String sessionId() {
        return sessionId;
    }

    public void attachClientChannel(QuicChannel channel) {
        this.clientChannel = channel;
    }

    public QuicChannel clientChannel() {
        return clientChannel;
    }

    public BackendServer connectedServer() {
        return connectedServer;
    }

    public void connectTo(BackendServer server) {
        this.connectedServer = server;
    }
}