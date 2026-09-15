package com.cairui.finreport.mapping;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColumnMappingRepository extends JpaRepository<ColumnMapping, Long> {
    List<ColumnMapping> findAllByOrderByImportTypeAsc();
    Optional<ColumnMapping> findFirstByImportTypeAndIsDefaultTrue(String importType);
}
