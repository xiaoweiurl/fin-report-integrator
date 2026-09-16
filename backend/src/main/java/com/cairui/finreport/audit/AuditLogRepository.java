package com.cairui.finreport.audit;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditLogRepository {
    int insert(AuditLog log);

    List<AuditLog> page(@Param("limit") int limit, @Param("offset") int offset);

    long count();
}
