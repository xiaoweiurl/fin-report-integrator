package com.cairui.finreport.importdata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImportExceptionRepository extends JpaRepository<ImportExceptionRow, Long> {
    List<ImportExceptionRow> findByBatchIdOrderByRowNumberAsc(Long batchId);
}
