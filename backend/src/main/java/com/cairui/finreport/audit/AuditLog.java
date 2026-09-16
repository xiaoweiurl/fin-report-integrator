package com.cairui.finreport.audit;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuditLog {
    private Long id;
    private String action;
    private String entityType;
    private String entityId;
    private String detail;
    private String username;
    private LocalDateTime createdAt;
}
