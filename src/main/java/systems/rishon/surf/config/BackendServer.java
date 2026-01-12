package systems.rishon.surf.config;

public final class BackendServer {

    private final String name;
    private final String host;
    private final Long port;

    public BackendServer(String name, String host, Long port) {
        this.name = name;
        this.host = host;
        this.port = port;
    }

    public String name() {
        return name;
    }

    public String host() {
        return host;
    }

    public Long port() {
        return port;
    }
}