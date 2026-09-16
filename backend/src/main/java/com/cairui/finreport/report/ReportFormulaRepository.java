package com.cairui.finreport.report;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportFormulaRepository {
    List<ReportFormula> findByReportType(@Param("reportType") String reportType);

    int insert(ReportFormula formula);
}
