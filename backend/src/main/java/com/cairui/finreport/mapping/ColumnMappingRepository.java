package com.cairui.finreport.mapping;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ColumnMappingRepository {
    List<ColumnMapping> findAllByOrderByImportTypeAsc();

    Optional<ColumnMapping> findById(@Param("id") Long id);

    Optional<ColumnMapping> findFirstByImportTypeAndIsDefaultTrue(@Param("importType") String importType);

    long count();

    int insert(ColumnMapping mapping);

    int update(ColumnMapping mapping);
}
