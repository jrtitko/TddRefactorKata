package com.industriousgnomes.tddrefactorkata.service

import com.industriousgnomes.tddrefactorkata.exceptions.InvalidSourceException
import com.industriousgnomes.tddrefactorkata.factory.SchemasFactory
import com.industriousgnomes.tddrefactorkata.model.Schema
import com.industriousgnomes.tddrefactorkata.mongo.SchemaRecorder
import spock.lang.Specification
import spock.lang.Subject

class DataPortalTest extends Specification {

    @Subject
    DataPortal dataPortal

    SchemaRecorder schemaRecorder = Mock()

    SchemasFactory schemasFactory = Mock()

    void setup() {
        dataPortal = new DataPortal(
                schemaRecorder: schemaRecorder,
                schemasFactory: schemasFactory
        )
    }

    def "Should throw an InvalidSourceException unknown datasource"() {
        given:
            schemasFactory.getSchemas() >> { throw new InvalidSourceException() }

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
