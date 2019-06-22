package com.industriousgnomes.tddrefactorkata.factory


import com.industriousgnomes.tddrefactorkata.exceptions.InvalidSourceException
import spock.lang.Ignore
import spock.lang.Specification

class SchemasFactoryTest extends Specification {

    @Ignore("Code moved to SchemaFactory")
    def "Should throw an InvalidSourceException if unknown datasource"() {
        given:
            System.setProperty("datasource.name", "unknown");

        when:
            dataPortal.copyDataOver()

        then:
            thrown InvalidSourceException
    }


}
