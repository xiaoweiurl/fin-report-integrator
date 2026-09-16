package com.cairui.finreport.report;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ReportFormula {
    private Long id;
    private String reportType;
    private String lineCode;
    private String accountCode;
    private BigDecimal multiplier = BigDecimal.ONE;
}
