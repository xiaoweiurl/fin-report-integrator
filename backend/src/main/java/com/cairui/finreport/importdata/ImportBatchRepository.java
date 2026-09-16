package com.cairui.finreport.importdata;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ImportBatchRepository {
    List<ImportBatch> findAllByOrderByCreatedAtDesc();

    List<ImportBatch> findByPeriodOrderByCreatedAtDesc(@Param("period") String period);

    Optional<ImportBatch> findById(@Param("id") Long id);

    Optional<ImportBatch> findFirstByFileHashAndImportTypeAndPeriod(
            @Param("fileHash") String fileHash,
            @Param("importType") String importType,
            @Param("period") String period);

    int insert(ImportBatch batch);

    int update(ImportBatch batch);
}
