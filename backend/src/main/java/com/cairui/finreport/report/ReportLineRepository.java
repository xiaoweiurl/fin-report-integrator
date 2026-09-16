package com.cairui.finreport.report;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReportLineRepository {
    List<ReportLine> findByReportTypeOrderBySortOrderAsc(@Param("reportType") String reportType);

    long count();

    int insert(ReportLine line);
}
