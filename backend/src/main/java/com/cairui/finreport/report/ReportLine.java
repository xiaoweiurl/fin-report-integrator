package com.cairui.finreport.report;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportLine {
    private Long id;
    private String reportType;
    private String lineCode;
    private String lineName;
    private String section;
    private String lineKind;
    private String formulaExpr;
    private Integer sortOrder;
    private Integer indent = 0;
}
