package com.cairui.finreport.importdata;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ImportBatch {
    private Long id;
    private String importType;
    private String fileName;
    private String fileHash;
    private String period;
    private String status;
    private Integer totalRows = 0;
    private Integer successRows = 0;
    private Integer errorRows = 0;
    private String message;
    private String createdBy;
    private LocalDateTime createdAt;
}
