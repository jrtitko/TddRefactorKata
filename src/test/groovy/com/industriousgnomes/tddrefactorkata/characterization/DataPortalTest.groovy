package com.industriousgnomes.tddrefactorkata.characterization

import com.industriousgnomes.tddrefactorkata.cassandra.CassandraConnector
import com.industriousgnomes.tddrefactorkata.mongo.MongoConnector
import com.industriousgnomes.tddrefactorkata.service.DataPortal
import com.mongodb.MongoClient
import com.mongodb.MongoCredential
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

/*
    WARNING!
    If you run these tests and they fail with a NoHostAvailableException, then you will need to
    start the Cassandra V2, Cassandra V3, and Mongo servers in docker.  See DockerSetup.md for details.
 */

class DataPortalTest extends Specification {

    @Subject
    DataPortal dataPortal

    @Shared
    MongoCollection<Document> collection

    MongoConnector mongoConnector

    CassandraConnector cassandraConnector

    def setupSpec() {
        MongoClient mongo = new MongoClient("localhost", 27017)
        MongoCredential.createCredential("sampleUser", "myDb", "password".toCharArray())
        MongoDatabase database = mongo.getDatabase("myDb")
        collection = database.getCollection("schemaData")
        collection.drop()
    }

    def setup() {
        System.setProperty("mongo.cluster", "localhost");
        System.setProperty("mongo.port", "27017");
        System.setProperty("mongo.user", "sampleUser");
        System.setProperty("mongo.password", "password");

        collection.drop()
    }

    // Normally we would wire up the DataPortal in the setup() but the various tests
    // must setup different system properties, so we cant instantiate the DataPortal
    // until after those properties are set.
    private void setupWiring() {
        mongoConnector = new MongoConnector()
        cassandraConnector = new CassandraConnector()

        dataPortal = new DataPortal(
                mongoConnector: mongoConnector,
                cassandraConnector: cassandraConnector
        )
    }

    def "run copyDataOver against cassandra-v2"() {
        given:
            System.setProperty("cassandra.cluster", "localhost");
            System.setProperty("cassandra.port", "9043");
            System.setProperty("cassandra.user", "someuser");
            System.setProperty("cassandra.password", "somepassword");
            System.setProperty("datasource.name", "system.schema_columns");

            System.setProperty("dataportal.keyspace", "system_auth")

            setupWiring()

        when:
            dataPortal.copyDataOver()

        then:
            noExceptionThrown()
            FindIterable<Document> documents = collection.find()
            documents.size() == 1
            documents.first().containsKey("system_auth")
    }

    def "run copyDataOver against cassandra-v3"() {
        given:
            System.setProperty("cassandra.cluster", "localhost");
            System.setProperty("cassandra.port", "9042");
            System.setProperty("cassandra.user", "someuser");
            System.setProperty("cassandra.password", "somepassword");
            System.setProperty("datasource.name", "system_schema.columns");

            System.setProperty("dataportal.keyspace", "system_auth")

            setupWiring()

        when:
            dataPortal.copyDataOver()

        then:
            noExceptionThrown()
            FindIterable<Document> documents = collection.find()
            documents.size() == 1
            documents.first().containsKey("system_auth")
    }
}
