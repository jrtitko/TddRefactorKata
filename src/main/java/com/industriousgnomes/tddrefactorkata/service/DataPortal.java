package com.industriousgnomes.tddrefactorkata.service;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.Mapper;
import com.datastax.driver.mapping.MappingManager;
import com.datastax.driver.mapping.Result;
import com.datastax.driver.mapping.annotations.Table;
import com.industriousgnomes.tddrefactorkata.mongo.MongoConnector;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.util.HashMap;

@Service
public class DataPortal {

    @Autowired
    MongoConnector mongoConnector;

    public void copyDataOver() {

        // Logger
        Logger logger = LoggerFactory.getLogger(DataPortal.class);

        // Pull configuration from props
        String endpoint = System.getProperty("cassandra.cluster");
        Integer port = Integer.getInteger("cassandra.port", 9042);
        String username = System.getProperty("cassandra.user");
        String password = System.getProperty("cassandra.password");
        String cassandra_column_table = System.getProperty("cassandra.column_table");
        String keyspace = System.getProperty("dataportal.keyspace");

        // Setup Cassandra Connection
        CassandraConnector connector = new CassandraConnector();
        connector.connect(endpoint, port, username, password);
        Session session = connector.getSession();

        MappingManager manager = new MappingManager(session);

        // query system tables for colun keyspace/table/column info
        // we can derive keyspaces, tables, and columns from this single table
        HashMap<String, HashMap> schemaInfo = new HashMap<String, HashMap>();

        // query for 2.x hosts
        if (cassandra_column_table.equals("system.schema_columns")) {
            PreparedStatement query = session.prepare("SELECT * FROM system.schema_columns WHERE keyspace_name=?");
            ResultSet rs = session.execute(query.bind(keyspace));

            Mapper<SchemaColumn> mapper = manager.mapper(SchemaColumn.class);
            Result<SchemaColumn> rows = mapper.map(rs);

            // fetch and store schema info
            rows.forEach(r -> {
                String keyspace_name = r.getKeyspace_name();
                String table_name = r.getColumnfamily_name();
                String column_name = r.getColumn_name();
                String column_type = r.getValidator();

                //create structures for keyspace and table if required
                schemaInfo.putIfAbsent(keyspace_name, new HashMap<String, HashMap>());
                HashMap<String, HashMap<String, String>> keyspace_map = schemaInfo.get(keyspace_name);
                keyspace_map.putIfAbsent(table_name, new HashMap<String, String>());
                HashMap<String, String> table_map = keyspace_map.get(table_name);
                String cleaned_column_type = column_type.replaceAll("org.apache.cassandra.db.marshal.", "");
                table_map.put(column_name, cleaned_column_type);
            });
        } else if (cassandra_column_table.equals("system_schema.columns")) {    // query for 3.x hosts
            PreparedStatement query = session.prepare("SELECT * FROM system_schema.columns WHERE keyspace_name=?");
            ResultSet rs = session.execute(query.bind(keyspace));

            Mapper<Column> mapper = manager.mapper(Column.class);
            Result<Column> rows = mapper.map(rs);

            // fetch and store schema info
            rows.forEach(r -> {
                String keyspace_name = r.getKeyspace_name();
                String table_name = r.getTable_name();
                String column_name = r.getColumn_name();
                String column_type = r.getType();

                //create structures for keyspace and table if required
                schemaInfo.putIfAbsent(keyspace_name, new HashMap<String, HashMap>());
                HashMap<String, HashMap<String, String>> keyspace_map = schemaInfo.get(keyspace_name);
                keyspace_map.putIfAbsent(table_name, new HashMap<String, String>());
                HashMap<String, String> table_map = keyspace_map.get(table_name);
                table_map.put(column_name, column_type);
            });
        } else {
            System.exit(1);
        }

        connector.close();


        try {
            mongoConnector.connect();
            MongoCollection<Document> collection = mongoConnector.getCollection("schemaData");
            collection.drop();

            for (HashMap.Entry<String, HashMap> keyspace_entry : schemaInfo.entrySet()) {
                Document documentKeyspace = new Document();

                HashMap<String, HashMap> tables = keyspace_entry.getValue();
                Document documentTables = new Document();
                for (HashMap.Entry<String, HashMap> table_entry : tables.entrySet()) {

                    HashMap<String, String> columns = table_entry.getValue();
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

// Cassandra V2
@Table(keyspace = "system", name = "schema_columns")
class SchemaColumn {
    private String keyspace_name;
    private String columnfamily_name;
    private String column_name;
    private int component_index;
    private String index_name;
    private String index_options;
    private String index_type;
    private String type;
    private String validator;
    public String getKeyspace_name() { return this.keyspace_name; }
    public String getColumnfamily_name() { return this.columnfamily_name; }
    public String getColumn_name() { return this.column_name; }
    public int getComponent_index() { return this.component_index; }
    public String getIndex_name() { return this.index_name; }
    public String getIndex_options() { return this.index_options; }
    public String getIndex_type() { return this.index_type; }
    public String getType() { return this.type; }
    public String getValidator() { return this.validator; }
}

// Cassandra v3
@Table(keyspace = "system_schema", name = "columns")
class Column {
    private String keyspace_name;
    private String table_name;
    private String column_name;
    private String clustering_order;
    private ByteBuffer column_name_bytes;
    private String kind;
    private int position;
    private String type;
    public String getKeyspace_name() { return this.keyspace_name; }
    public String getTable_name() { return this.table_name; }
    public String getColumn_name() { return this.column_name; }
    public String getClustering_order() { return this.clustering_order; }
    public ByteBuffer getColumn_name_bytes() { return this.column_name_bytes; }
    public String getKind() { return this.kind; }
    public int getPosition() { return this.position; }
    public String getType() { return this.type; }
}

@Table(keyspace = "system", name = "schema_keyspaces")
class SchemaKeyspace {
    private String keyspace_name;
    private Boolean durable_writes;
    private String strategy_class;
    private String strategy_options;
    public String getKeyspace_name() { return this.keyspace_name; };
    public Boolean getDurable_writes() { return this.durable_writes; };
    public String getStrategy_class() { return this.strategy_class; };
    public String getStrategy_options() { return this.strategy_options; };
}
