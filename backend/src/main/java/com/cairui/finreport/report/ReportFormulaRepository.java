package com.cairui.finreport.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportFormulaRepository extends JpaRepository<ReportFormula, Long> {
    List<ReportFormula> findByReportType(String reportType);
}
