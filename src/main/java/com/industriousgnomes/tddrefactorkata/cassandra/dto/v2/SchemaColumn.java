package com.industriousgnomes.tddrefactorkata.cassandra.dto.v2;

import com.datastax.driver.mapping.annotations.Table;
import lombok.Getter;

@Table(keyspace = "system", name = "schema_columns")
@Getter
public class SchemaColumn {
    private String keyspace_name;
    private String columnfamily_name;
    private String column_name;
    private String validator;
}
