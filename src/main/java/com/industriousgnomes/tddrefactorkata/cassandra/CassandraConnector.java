package com.industriousgnomes.tddrefactorkata.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CassandraConnector {

    private Cluster cluster;
    private Session session;

    public void connect(String node, Integer port, String username, String password) {
        Cluster.Builder b = Cluster.builder()
                                    .addContactPoint(node)
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
