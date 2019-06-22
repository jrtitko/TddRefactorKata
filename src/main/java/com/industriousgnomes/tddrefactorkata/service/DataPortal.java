package com.industriousgnomes.tddrefactorkata.service;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.industriousgnomes.tddrefactorkata.cassandra.CassandraConnector;
import com.industriousgnomes.tddrefactorkata.cassandra.dto.v2.SchemaColumn;
import com.industriousgnomes.tddrefactorkata.cassandra.dto.v3.Column;
import com.industriousgnomes.tddrefactorkata.exceptions.InvalidSourceException;
import com.industriousgnomes.tddrefactorkata.model.Schema;
import com.industriousgnomes.tddrefactorkata.mongo.MongoConnector;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Service
public class DataPortal {

    @Autowired
    MongoConnector mongoConnector;

    @Autowired
    CassandraConnector cassandraConnector;

    public void copyDataOver() {

        // Logger
        Logger logger = LoggerFactory.getLogger(DataPortal.class);

        // Pull configuration from props
        String datasourceName = System.getProperty("datasource.name");
        String keyspace = System.getProperty("dataportal.keyspace");

        // query system tables for colun keyspace/table/column info
        // we can derive keyspaces, tables, and columns from this single table
        Map<String, Map<String, Map<String, String>>> schemaInfo = new HashMap<>();

        if ("cassandraV2".equals(datasourceName)) {
            cassandraConnector.connect();
            Session session = cassandraConnector.getSession();

            MappingManager manager = new MappingManager(session);

            PreparedStatement query = session.prepare("SELECT * FROM system.schema_columns WHERE keyspace_name=?");
            ResultSet rs = session.execute(query.bind(keyspace));

            Mapper<SchemaColumn> mapper = manager.mapper(SchemaColumn.class);
            Result<SchemaColumn> rows = mapper.map(rs);

            Collection<Schema> schemas = new LinkedList<>();
            rows.forEach(r -> {
                schemas.add(Schema.builder()
                    .keyspaceName(r.getKeyspace_name())
                    .tableName(r.getColumnfamily_name())
                    .columnName(r.getColumn_name())
                    .columnType(r.getValidator().replaceAll("org.apache.cassandra.db.marshal.", ""))
                    .build()
                );
            });

            processSchemaInfo(schemaInfo, schemas);

            cassandraConnector.close();

        } else if ("cassandraV3".equals(datasourceName)) {
            cassandraConnector.connect();
            Session session = cassandraConnector.getSession();

            MappingManager manager = new MappingManager(session);

            PreparedStatement query = session.prepare("SELECT * FROM system_schema.columns WHERE keyspace_name=?");
            ResultSet rs = session.execute(query.bind(keyspace));

            Mapper<Column> mapper = manager.mapper(Column.class);
            Result<Column> rows = mapper.map(rs);

            Collection<Schema> schemas = new LinkedList<>();
            rows.forEach(r -> {
                schemas.add(Schema.builder()
                    .keyspaceName(r.getKeyspace_name())
                    .tableName(r.getTable_name())
                    .columnName(r.getColumn_name())
                    .columnType(r.getType())
                                    .build()
                );
            });

            processSchemaInfo(schemaInfo, schemas);

            cassandraConnector.close();

        } else {
            throw new InvalidSourceException();
        }

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

    private void processSchemaInfo(Map<String, Map<String, Map<String, String>>> schemaInfo, Collection<Schema> schemas) {
        schemas.forEach(schema -> {
            putSchemaInfoInMap(schemaInfo,
                    schema.getKeyspaceName(),
                    schema.getTableName(),
                    schema.getColumnName(),
                    schema.getColumnType());
        });
    }

    private void putSchemaInfoInMap(Map<String, Map<String, Map<String, String>>> schemaInfo, String keyspaceName, String tableName, String columnName, String columnType) {
        schemaInfo.putIfAbsent(keyspaceName, new HashMap<>());
        Map<String, Map<String, String>> keyspace_map = schemaInfo.get(keyspaceName);
        keyspace_map.putIfAbsent(tableName, new HashMap<>());
        Map<String, String> table_map = keyspace_map.get(tableName);
        table_map.put(columnName, columnType);
    }
}

