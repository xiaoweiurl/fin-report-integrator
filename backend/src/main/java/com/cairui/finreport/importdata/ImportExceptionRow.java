package com.cairui.finreport.importdata;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportExceptionRow {
    private Long id;
    private Long batchId;
    private Integer rowNumber;
    private String rawJson;
    private String errorMessage;
}
