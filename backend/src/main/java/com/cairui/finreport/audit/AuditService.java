package com.cairui.finreport.audit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AuditService {
    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public void log(String action, String entityType, String entityId, String detail, String username) {
        AuditLog row = new AuditLog();
        row.setAction(action);
        row.setEntityType(entityType);
        row.setEntityId(entityId);
        row.setDetail(detail);
        row.setUsername(username);
        repo.insert(row);
    }

    public Map<String, Object> page(int page, int size) {
        int limit = Math.max(size, 1);
        int offset = Math.max(page, 0) * limit;
        List<AuditLog> content = repo.page(limit, offset);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("content", content);
        body.put("totalElements", repo.count());
        return body;
    }
}
