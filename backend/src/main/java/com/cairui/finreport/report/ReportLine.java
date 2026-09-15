package com.cairui.finreport.report;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "report_lines")
public class ReportLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_type", nullable = false, length = 40)
    private String reportType;

    @Column(name = "line_code", nullable = false, length = 32)
    private String lineCode;

    @Column(name = "line_name", nullable = false, length = 200)
    private String lineName;

    @Column(length = 64)
    private String section;

    @Column(name = "line_kind", nullable = false, length = 16)
    private String lineKind;

    @Column(name = "formula_expr", length = 500)
    private String formulaExpr;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Integer indent = 0;
}
