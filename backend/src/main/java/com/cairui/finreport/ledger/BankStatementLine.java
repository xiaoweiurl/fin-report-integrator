package com.cairui.finreport.ledger;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class BankStatementLine {
    private Long id;
    private Long batchId;
    private String period;
    private LocalDate txnDate;
    private BigDecimal amount;
    private String direction;
    private String counterparty;
    private String summary;
    private String bankAccount;
    private String refNo;
    private BigDecimal balanceAfter;
    private Boolean matched = false;
}
