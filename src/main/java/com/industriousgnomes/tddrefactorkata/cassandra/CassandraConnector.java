package com.industriousgnomes.tddrefactorkata.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConnector {

    private Cluster cluster;
    private Session session;

    private String endpoint;
    private Integer port;
    private String username;
    private String password;

    public CassandraConnector() {
        endpoint = System.getProperty("cassandra.cluster");
        port = Integer.getInteger("cassandra.port", 9042);
        username = System.getProperty("cassandra.user");
        password = System.getProperty("cassandra.password");
    }

    public void connect() {
        Cluster.Builder b = Cluster.builder()
                                    .addContactPoint(endpoint)
                                    .withCredentials(username, password)
                                    .withoutJMXReporting();

        if (port != null) {
            b.withPort(port);
        }

        cluster = b.build();
        session = cluster.connect();
    }

    public Session getSession() {
        return session;
    }

    public void close() {
        session.close();
        cluster.close();
    }
}
