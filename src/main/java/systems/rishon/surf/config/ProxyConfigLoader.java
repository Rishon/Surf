package systems.rishon.surf.config;

import org.tomlj.Toml;
import org.tomlj.TomlArray;
import org.tomlj.TomlParseResult;
import org.tomlj.TomlTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ProxyConfigLoader {

    public static ProxyConfig load(InputStream input) {
        TomlParseResult toml;

        try {
            toml = Toml.parse(input);
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse config file", e);
        }

        TomlTable bind = toml.getTable("bind");
        if (bind == null) {
            throw new IllegalStateException("Missing 'bind' configuration section");
        }

        String bindHost = bind.getString("host");
        if (bindHost == null) {
            throw new IllegalStateException("'bind.host' is missing");
        }

        Long bindPort = bind.getLong("port");
        if (bindPort == null) {
            throw new IllegalStateException("'bind.port' is missing");
        }

        Map<String, BackendServer> servers = new HashMap<>();
        TomlTable serversTable = toml.getTable("servers");

        if (serversTable == null) {
            throw new IllegalStateException("Missing 'servers' configuration section");
        }

        for (String serverName : serversTable.keySet()) {
            TomlTable serverTable = serversTable.getTable(serverName);
            if (serverTable == null) {
                throw new IllegalStateException(
                        "Server '" + serverName + "' must be a table with 'host' and 'port'"
                );
            }

            String host = serverTable.getString("host");
            if (host == null) {
                throw new IllegalStateException("Server '" + serverName + "' is missing 'host'");
            }

            Long port = serverTable.getLong("port");
            if (port == null) {
                throw new IllegalStateException("Server '" + serverName + "' is missing 'port'");
            }

            servers.put(serverName, new BackendServer(serverName, host, port));
        }

        TomlTable settings = toml.getTable("settings");
        if (settings == null) {
            throw new IllegalStateException("Missing 'settings' section");
        }

        TomlArray joinOrderArray = settings.getArray("join-order");
        if (joinOrderArray == null) {
            throw new IllegalStateException("Missing 'join-order' in settings section");
        }

        List<String> joinOrder = joinOrderArray.toList().stream()
                .map(Object::toString)
                .toList();

        return new ProxyConfig(bindHost, bindPort, servers, joinOrder);
    }
}
