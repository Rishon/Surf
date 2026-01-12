package systems.rishon.surf.routing;

import systems.rishon.surf.config.BackendServer;
import systems.rishon.surf.config.ProxyConfig;

public final class JoinServerRouter {

    private final ProxyConfig config;

    public JoinServerRouter(ProxyConfig config) {
        this.config = config;
    }

    public BackendServer selectInitialServer() {
        if (config.joinServers().isEmpty()) {
            throw new IllegalStateException("No join-servers configured");
        }

        String serverName = config.joinServers().getFirst();
        BackendServer server = config.servers().get(serverName);

        if (server == null) {
            throw new IllegalStateException(
                    "Join server '" + serverName + "' is not registered"
            );
        }

        return server;
    }
}
