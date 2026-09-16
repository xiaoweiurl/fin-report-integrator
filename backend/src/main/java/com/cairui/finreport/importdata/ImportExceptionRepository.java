package com.cairui.finreport.importdata;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ImportExceptionRepository {
    List<ImportExceptionRow> findByBatchIdOrderByRowNumberAsc(@Param("batchId") Long batchId);

    int insert(ImportExceptionRow row);
}
