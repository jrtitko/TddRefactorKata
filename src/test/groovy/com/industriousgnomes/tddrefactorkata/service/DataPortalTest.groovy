package com.industriousgnomes.tddrefactorkata.service


import com.industriousgnomes.tddrefactorkata.cassandra.CassandraConnector
import com.industriousgnomes.tddrefactorkata.exceptions.InvalidSourceException
import com.industriousgnomes.tddrefactorkata.model.Schema
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
            System.setProperty("datasource.name", "unknown");

        when:
            dataPortal.copyDataOver()

        then:
            thrown InvalidSourceException
    }

    def "Should put schema info into the schemaInfo map"() {
        given:
            Map<String, Map<String, Map<String, String>>> schemaInfo = new HashMap<>()
            String keyspaceName = "keyspaceName"
            String tableName = "tableName"
            String columnName = "columnName"
            String columnType = "columnType"

            Collection<Schema> schemas = Arrays.asList(
                    new Schema(keyspaceName,
                            tableName,
                            columnName,
                            columnType
                    )
            )

        when:
            dataPortal.processSchemaInfo(schemaInfo, schemas)

        then:
            schemaInfo.containsKey(keyspaceName)
            schemaInfo.get(keyspaceName).containsKey(tableName)
            schemaInfo.get(keyspaceName).get(tableName).containsKey(columnName)
            schemaInfo.get(keyspaceName).get(tableName).get(columnName) == columnType
    }
}
