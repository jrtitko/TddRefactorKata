package com.industriousgnomes.tddrefactorkata.factory


import com.industriousgnomes.tddrefactorkata.cassandra.CassandraConnector
import com.industriousgnomes.tddrefactorkata.exceptions.InvalidSourceException
import spock.lang.Specification
import spock.lang.Subject

class SchemasFactoryTest extends Specification {

    @Subject
    SchemasFactory schemasFactory

    CassandraConnector cassandraConnector = Mock()

    void setup() {
        schemasFactory = new SchemasFactory(
                cassandraConnector: cassandraConnector
        )
    }

    def "Should throw an InvalidSourceException if unknown datasource"() {
        given:
            System.setProperty("datasource.name", "unknown");

        when:
            schemasFactory.getSchemas()

        then:
            thrown InvalidSourceException
    }
}
