package com.industriousgnomes.tddrefactorkata.cassandra

import spock.lang.Specification
import spock.lang.Subject

class CassandraConnectorTest extends Specification {

    @Subject
    CassandraConnector cassandraConnector

    void setup() {
        System.setProperty("cassandra.cluster", "localhost");
        System.setProperty("cassandra.port", "9042");
        System.setProperty("cassandra.user", "someuser");
        System.setProperty("cassandra.password", "somepassword");

        cassandraConnector = new CassandraConnector()
    }

    def "Should connect and close the connection to Cassandra"() {
        given:
            //

        when:
            cassandraConnector.connect()

        then:
            noExceptionThrown()
            !cassandraConnector.getSession().closed
            !cassandraConnector.cluster.closed

        when:
            cassandraConnector.close()

        then:
            noExceptionThrown()
            cassandraConnector.getSession().closed
            cassandraConnector.cluster.closed
    }

    def "Should not have a session if not connected to Cassandra"() {
        given:
            //

        when:
            def session = cassandraConnector.getSession()

        then:
            session == null
    }
}
