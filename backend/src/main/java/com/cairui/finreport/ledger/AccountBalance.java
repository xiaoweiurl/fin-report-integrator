package com.cairui.finreport.ledger;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountBalance {
    private Long id;
    private Long batchId;
    private String period;
    private String accountCode;
    private String accountName;
    private BigDecimal openingDebit = BigDecimal.ZERO;
    private BigDecimal openingCredit = BigDecimal.ZERO;
    private BigDecimal periodDebit = BigDecimal.ZERO;
    private BigDecimal periodCredit = BigDecimal.ZERO;
    private BigDecimal closingDebit = BigDecimal.ZERO;
    private BigDecimal closingCredit = BigDecimal.ZERO;
    private String partnerCode;
}
