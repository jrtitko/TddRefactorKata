package com.industriousgnomes.tddrefactorkata.cassandra.dto.v3;

import com.datastax.driver.mapping.annotations.Table;
import lombok.Getter;

@Table(keyspace = "system_schema", name = "columns")
@Getter
public class Column {
    private String keyspace_name;
    private String table_name;
    private String column_name;
    private String type;
}
