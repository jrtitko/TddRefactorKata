package com.industriousgnomes.tddrefactorkata.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Schema {
    private String keyspace_name;
    private String table_name;
    private String column_name;
    private String column_type;
}
