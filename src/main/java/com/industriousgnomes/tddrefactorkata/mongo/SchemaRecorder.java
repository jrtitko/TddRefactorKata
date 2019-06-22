package com.industriousgnomes.tddrefactorkata.mongo;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SchemaRecorder {

    Logger logger = LoggerFactory.getLogger(SchemaRecorder.class);

    @Autowired
    MongoConnector mongoConnector;

    public void recordSchema(Map<String, Map<String, Map<String, String>>> schemaInfo) {
        try {
            mongoConnector.connect();
            MongoCollection<Document> collection = mongoConnector.getCollection("schemaData");
            collection.drop();

            for (Map.Entry<String, Map<String, Map<String, String>>> keyspace_entry : schemaInfo.entrySet()) {
                Document documentKeyspace = new Document();

                Map<String, Map<String, String>> tables = keyspace_entry.getValue();
                Document documentTables = new Document();
                for (Map.Entry<String, Map<String, String>> table_entry : tables.entrySet()) {

                    Map<String, String> columns = table_entry.getValue();
                    Document documentColumns = new Document();
                    for (HashMap.Entry<String, String> column_entry: columns.entrySet()) {
                        documentColumns.append(column_entry.getKey(), column_entry.getValue());
                    }

                    documentTables.append(table_entry.getKey(), documentColumns);
                }

                documentKeyspace.append(keyspace_entry.getKey(), documentTables);
                collection.insertOne(documentKeyspace);
            }

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }
}
