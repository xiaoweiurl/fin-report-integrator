package com.cairui.finreport.ledger;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "account_balances")
public class AccountBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(nullable = false, length = 16)
    private String period;

    @Column(name = "account_code", nullable = false, length = 32)
    private String accountCode;

    @Column(name = "account_name", length = 200)
    private String accountName;

    @Column(name = "opening_debit", nullable = false, precision = 18, scale = 2)
    private BigDecimal openingDebit = BigDecimal.ZERO;

    @Column(name = "opening_credit", nullable = false, precision = 18, scale = 2)
    private BigDecimal openingCredit = BigDecimal.ZERO;

    @Column(name = "period_debit", nullable = false, precision = 18, scale = 2)
    private BigDecimal periodDebit = BigDecimal.ZERO;

    @Column(name = "period_credit", nullable = false, precision = 18, scale = 2)
    private BigDecimal periodCredit = BigDecimal.ZERO;

    @Column(name = "closing_debit", nullable = false, precision = 18, scale = 2)
    private BigDecimal closingDebit = BigDecimal.ZERO;

    @Column(name = "closing_credit", nullable = false, precision = 18, scale = 2)
    private BigDecimal closingCredit = BigDecimal.ZERO;

    @Column(name = "partner_code", length = 32)
    private String partnerCode;
}
