package com.cairui.finreport.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "report_formulas")
public class ReportFormula {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type", nullable = false, length = 40)
    private String reportType;

    @Column(name = "line_code", nullable = false, length = 32)
    private String lineCode;

    @Column(name = "account_code", nullable = false, length = 32)
    private String accountCode;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal multiplier = BigDecimal.ONE;
}
