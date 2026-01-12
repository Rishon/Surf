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
        Long bindPort = bind.getLong("port");

        Map<String, BackendServer> servers = new HashMap<>();
        TomlTable serversTable = toml.getTable("servers");

        if (serversTable == null) {
            throw new IllegalStateException("Missing 'servers' configuration section");
        }

        for (String serverName : serversTable.keySet()) {
            TomlTable server = serversTable.getTable(serverName);
            if (server == null) {
                throw new IllegalStateException(
                        "Server '" + serverName + "' configuration is invalid"
                );
            }

            servers.put(
                    serverName,
                    new BackendServer(
                            serverName,
                            server.getString("host"),
                            server.getLong("port")
                    )
            );
        }

        TomlArray joinOrderArray = toml.getArray("join-order");
        if (joinOrderArray == null) {
            throw new IllegalStateException("Missing 'join-order' configuration section");
        }

        List<String> joinOrder = joinOrderArray.toList().stream()
                .map(Object::toString)
                .toList();

        return new ProxyConfig(
                bindHost,
                bindPort,
                servers,
                joinOrder
        );
    }
}
