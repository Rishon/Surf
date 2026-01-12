package systems.rishon.surf.config;

import java.util.List;
import java.util.Map;

public final class ProxyConfig {

    private final String bindHost;
    private final Long bindPort;

    private final Map<String, BackendServer> servers;
    private final List<String> joinServers;

    public ProxyConfig(
            String bindHost,
            Long bindPort,
            Map<String, BackendServer> servers,
            List<String> joinServers
    ) {
        this.bindHost = bindHost;
        this.bindPort = bindPort;
        this.servers = servers;
        this.joinServers = joinServers;
    }

    public String bindHost() {
        return bindHost;
    }

    public Long bindPort() {
        return bindPort;
    }

    public Map<String, BackendServer> servers() {
        return servers;
    }

    public List<String> joinServers() {
        return joinServers;
    }
}