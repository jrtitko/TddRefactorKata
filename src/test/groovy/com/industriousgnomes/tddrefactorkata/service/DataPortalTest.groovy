package com.industriousgnomes.tddrefactorkata.service


import com.industriousgnomes.tddrefactorkata.cassandra.CassandraConnector
import com.industriousgnomes.tddrefactorkata.exceptions.InvalidSourceException
import com.industriousgnomes.tddrefactorkata.mongo.MongoConnector
import spock.lang.Specification
import spock.lang.Subject

class DataPortalTest extends Specification {

    @Subject
    DataPortal dataPortal

    MongoConnector mongoConnector = Mock()

    CassandraConnector cassandraConnector = Mock()

    void setup() {
        dataPortal = new DataPortal(
                mongoConnector: mongoConnector,
                cassandraConnector: cassandraConnector
        )
    }

    def "Should throw an InvalidSourceException if unknown datasource"() {
        given:
            System.setProperty("cassandra.column_table", "unknown");

        when:
            dataPortal.copyDataOver()

        then:
            thrown InvalidSourceException
    }
}
