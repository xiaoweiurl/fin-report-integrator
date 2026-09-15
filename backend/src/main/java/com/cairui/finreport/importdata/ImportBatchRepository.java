package com.cairui.finreport.importdata;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImportBatchRepository extends JpaRepository<ImportBatch, Long> {
    List<ImportBatch> findAllByOrderByCreatedAtDesc();
    List<ImportBatch> findByPeriodOrderByCreatedAtDesc(String period);
    Optional<ImportBatch> findFirstByFileHashAndImportTypeAndPeriod(String fileHash, String importType, String period);
    Optional<ImportBatch> findFirstByImportTypeAndPeriodAndStatusInOrderByCreatedAtDesc(
            String importType, String period, List<String> statuses);
}
