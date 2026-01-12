package systems.rishon.surf;

import io.netty.incubator.codec.quic.QuicChannel;
import lombok.Getter;
import systems.rishon.surf.bootstrap.QuicProxyServer;
import systems.rishon.surf.config.BackendServer;
import systems.rishon.surf.config.ProxyConfig;
import systems.rishon.surf.routing.JoinServerRouter;
import systems.rishon.surf.session.ProxyPlayerSession;

import java.util.logging.Logger;

public class SurfProxy {

    @Getter
    private final Logger logger = Logger.getLogger("Surf");
    private final ProxyConfig config;
    private final JoinServerRouter joinRouter;

    public SurfProxy(ProxyConfig config) {
        this.config = config;
        this.joinRouter = new JoinServerRouter(config);
    }

    public void start() throws Exception {
        QuicProxyServer server = new QuicProxyServer(config, this);
        server.start();
    }

    public void onPlayerConnect(
            ProxyPlayerSession session,
            QuicChannel clientChannel
    ) {
        session.attachClientChannel(clientChannel);

        BackendServer target = joinRouter.selectInitialServer();
        session.connectTo(target);

        logger.info("Session " + session.sessionId()
                + " routed to join server "
                + target.name()
                + " (" + target.host() + ":" + target.port() + ")");
    }
}
