package com.industriousgnomes.tddrefactorkata.service;

import com.industriousgnomes.tddrefactorkata.factory.SchemasFactory;
import com.industriousgnomes.tddrefactorkata.model.Schema;
import com.industriousgnomes.tddrefactorkata.mongo.SchemaRecorder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class DataPortal {

    @Autowired
    SchemasFactory schemasFactory;

    @Autowired
    SchemaRecorder schemaRecorder;

    public void copyDataOver() {

        // query system tables for colun keyspace/table/column info
        // we can derive keyspaces, tables, and columns from this single table
        Map<String, Map<String, Map<String, String>>> schemaInfo = new HashMap<>();

        Collection<Schema> schemas = schemasFactory.getSchemas();

        processSchemaInfo(schemaInfo, schemas);

        schemaRecorder.recordSchema(schemaInfo);
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

