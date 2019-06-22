package com.industriousgnomes.tddrefactorkata.factory;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.LinkedList;

@Component
public class SchemasFactory {

    @Autowired
    CassandraConnector cassandraConnector;

    public Collection<Schema> getSchemas() {
        // Pull configuration from props
        String datasourceName = System.getProperty("datasource.name");
        String keyspace = System.getProperty("dataportal.keyspace");

        Collection<Schema> schemas = new LinkedList<>();
        if ("cassandraV2".equals(datasourceName)) {
            cassandraConnector.connect();
            Session session = cassandraConnector.getSession();

            MappingManager manager = new MappingManager(session);

            PreparedStatement query = session.prepare("SELECT * FROM system.schema_columns WHERE keyspace_name=?");
            ResultSet rs = session.execute(query.bind(keyspace));

            Mapper<SchemaColumn> mapper = manager.mapper(SchemaColumn.class);
            Result<SchemaColumn> rows = mapper.map(rs);

            rows.forEach(r -> {
                schemas.add(Schema.builder()
                                    .keyspaceName(r.getKeyspace_name())
                                    .tableName(r.getColumnfamily_name())
                                    .columnName(r.getColumn_name())
                                    .columnType(r.getValidator().replaceAll("org.apache.cassandra.db.marshal.", ""))
                                    .build()
                );
            });

            cassandraConnector.close();

        } else if ("cassandraV3".equals(datasourceName)) {
            cassandraConnector.connect();
            Session session = cassandraConnector.getSession();

            MappingManager manager = new MappingManager(session);

            PreparedStatement query = session.prepare("SELECT * FROM system_schema.columns WHERE keyspace_name=?");
            ResultSet rs = session.execute(query.bind(keyspace));

            Mapper<Column> mapper = manager.mapper(Column.class);
            Result<Column> rows = mapper.map(rs);

            rows.forEach(r -> {
                schemas.add(Schema.builder()
                                    .keyspaceName(r.getKeyspace_name())
                                    .tableName(r.getTable_name())
                                    .columnName(r.getColumn_name())
                                    .columnType(r.getType())
                                    .build()
                );
            });

            cassandraConnector.close();

        } else {
            throw new InvalidSourceException();
        }

        return schemas;
    }
}
