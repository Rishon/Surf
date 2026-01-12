package systems.rishon.surf;

import io.netty.incubator.codec.quic.QuicChannel;
import lombok.Getter;
import systems.rishon.surf.bootstrap.QuicProxyServer;
import systems.rishon.surf.config.BackendServer;
import systems.rishon.surf.config.ProxyConfig;
import systems.rishon.surf.config.ProxyConfigLoader;
import systems.rishon.surf.routing.JoinServerRouter;
import systems.rishon.surf.session.ProxyPlayerSession;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.logging.Logger;

public class SurfProxy {

    @Getter
    private final Logger logger = Logger.getLogger("Surf");
    private final ProxyConfig config;
    private final JoinServerRouter joinRouter;

    private QuicProxyServer server;

    public SurfProxy(ProxyConfig config) {
        this.config = config;
        this.joinRouter = new JoinServerRouter(config);
    }

    static void main(String[] args) throws Exception {
        try (InputStream inputStream = SurfProxy.class.getClassLoader().getResourceAsStream("surf.toml")) {
            if (inputStream == null) {
                throw new FileNotFoundException("surf.toml not found in resources");
            }
            ProxyConfig config = ProxyConfigLoader.load(inputStream);
            SurfProxy proxy = new SurfProxy(config);
            proxy.start();

            proxy.listenForShutdown(proxy.server);
        }
    }

    private void start() {
        this.server = new QuicProxyServer(config, this);
        this.server.start();
    }

    private void listenForShutdown(QuicProxyServer server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            this.logger.warning("SurfProxy shutting down");
            server.shutdown();
        }));
    }

    public void onPlayerConnect(
            ProxyPlayerSession session,
            QuicChannel clientChannel
    ) {
        session.attachClientChannel(clientChannel);

        BackendServer target = joinRouter.selectInitialServer();
        session.connectTo(target);

        this.logger.info("Session " + session.sessionId()
                + " routed to join server "
                + target.name()
                + " (" + target.host() + ":" + target.port() + ")");
    }
}
