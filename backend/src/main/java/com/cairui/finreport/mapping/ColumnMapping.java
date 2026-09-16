package com.cairui.finreport.mapping;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColumnMapping {
    private Long id;
    private String importType;
    private String name;
    private String mappingJson;
    private Boolean isDefault = false;
}
