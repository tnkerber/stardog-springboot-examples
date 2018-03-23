package com.polarisalpha.ca.stardog.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.docs.StardocsConnection;
import com.complexible.stardog.virtual.api.admin.VirtualGraphAdminConnection;

@Component
public class StardogConnectionService {
    @Value("${stardog.useEmbeddedServer}")
    private Boolean useEmbeddedServer;
    @Value("${stardog.remoteServer}")
    private String remoteServer;
    @Value("${stardog.user}")
    private String user;
    @Value("${stardog.password}")
    private String password;

    /**
     * Create an admin connection to the Stardog embedded or remote server.
     * Calling client is responsible for closing the connection.
     *
     * @return com.complexible.stardog.api.admin.AdminConnection
     */
    public final AdminConnection getAdminConnection() {
        if (Boolean.TRUE.equals(useEmbeddedServer)) {
            return AdminConnectionConfiguration
                    .toEmbeddedServer()
                    .credentials(user, password)
                    .connect();
        } else {
            return AdminConnectionConfiguration
                    .toServer(remoteServer)
                    .credentials(user, password)
                    .connect();
        }
    }

    public final VirtualGraphAdminConnection getVirtualGraphAdminConnection() {
        return getAdminConnection().as(VirtualGraphAdminConnection.class);
    }

    /**
     * Create a connection to the Stardog database.
     * Calling client is responsible for closing the connection.
     *
     * @return com.complexible.stardog.api.Connection
     */
    public final Connection getConnection(String dbName) {
        final ConnectionConfiguration connConfig = ConnectionConfiguration
                .to(dbName)
                .credentials(user, password);

        if (!Boolean.TRUE.equals(useEmbeddedServer)) {
            connConfig.server(remoteServer)
                    .reasoning(true);
        }

        return connConfig.connect();
    }

    /**
     * Create a connection to the Stardog database.
     * Calling client is responsible for closing the connection.
     *
     * @return com.complexible.stardog.docs.StardocsConnection
     */
    public final StardocsConnection getDocConnection(String dbName) {
        return getConnection(dbName).as(StardocsConnection.class);
    }

}
