package com.industriousgnomes.tddrefactorkata.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Schema {
    private String keyspaceName;
    private String tableName;
    private String columnName;
    private String columnType;
}
