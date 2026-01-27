package systems.rishon.surf;

import io.netty.incubator.codec.quic.Quic;
import lombok.Getter;
import systems.rishon.surf.bootstrap.QuicProxyServer;
import systems.rishon.surf.config.ProxyConfig;
import systems.rishon.surf.config.ProxyConfigLoader;
import systems.rishon.surf.routing.JoinServerRouter;
import systems.rishon.surf.session.ProxyPlayerSession;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.logging.Logger;

public class SurfProxy {

    @Getter
    private final Logger logger = Logger.getLogger("Surf");
    private final ProxyConfig config;
    @Getter
    private final JoinServerRouter joinRouter;

    @Getter
    private QuicProxyServer server;

    public SurfProxy(ProxyConfig config) {
        this.config = config;
        this.joinRouter = new JoinServerRouter(config);
    }

    static void main(String[] args) throws Exception {
        Quic.ensureAvailability();

        ProxyConfigLoader.generateConfig();

        try (InputStream in = Files.newInputStream(Path.of("surf.toml"))) {
            ProxyConfig config = ProxyConfigLoader.load(in);
            SurfProxy proxy = new SurfProxy(config);
            proxy.start();
            proxy.listenForShutdown();
        }
    }

    private void start() {
        this.server = new QuicProxyServer(config, this);
        this.server.start();
    }

    private void listenForShutdown() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equalsIgnoreCase("stop") || line.equalsIgnoreCase("shutdown")) {
                    shutdown();
                    break;
                }
            }
        }, "shutdown-listener").start();
    }

    private void shutdown() {
        logger.warning("SurfProxy shutting down");
        if (server != null) {
            server.shutdown();
        }
    }

    public void onPlayerDisconnect(ProxyPlayerSession session) {
        logger.info("Session " + session.getSessionId() + " disconnected");
    }

}