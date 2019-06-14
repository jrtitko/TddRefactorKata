package com.industriousgnomes.tddrefactorkata.mongo;

import com.mongodb.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConnector {

    private String mongo_endpoint;
    private Integer mongo_port;
    private String mongo_username;
    private String mongo_password;

    private MongoDatabase database;

    public MongoConnector() {
        mongo_endpoint = System.getProperty("mongo.cluster");
        mongo_port = Integer.getInteger("mongo.port", 27017);
        mongo_username = System.getProperty("mongo.user");
        mongo_password = System.getProperty("mongo.password");
    }

    public void connect() {
        MongoClient mongo = new MongoClient(mongo_endpoint, mongo_port);

        MongoCredential.createCredential(mongo_username, "myDb", mongo_password.toCharArray());

        database = mongo.getDatabase("myDb");

    }

    public MongoCollection<Document> getCollection(String name) {
        try {
            database.createCollection(name);
        } catch (MongoCommandException e) {
            // Table already created
        }

        return database.getCollection(name);
    }
}
