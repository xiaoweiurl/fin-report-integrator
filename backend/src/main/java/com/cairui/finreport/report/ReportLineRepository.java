package com.cairui.finreport.report;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportLineRepository extends JpaRepository<ReportLine, Long> {
    List<ReportLine> findByReportTypeOrderBySortOrderAsc(String reportType);
}
