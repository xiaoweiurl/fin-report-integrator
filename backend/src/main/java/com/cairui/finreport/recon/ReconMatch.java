package com.cairui.finreport.recon;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReconMatch {
    private Long id;
    private String period;
    private Long statementId;
    private Long journalId;
    private String matchType;
    private BigDecimal amount;
    private Integer dateDiffDays = 0;
    private LocalDateTime createdAt;
}
