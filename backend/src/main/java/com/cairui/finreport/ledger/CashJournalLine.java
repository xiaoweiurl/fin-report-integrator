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
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "cash_journal_lines")
public class CashJournalLine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(nullable = false, length = 16)
    private String period;

    @Column(name = "txn_date", nullable = false)
    private LocalDate txnDate;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 8)
    private String direction;

    @Column(name = "account_code", length = 32)
    private String accountCode;

    @Column(length = 200)
    private String counterparty;

    @Column(length = 500)
    private String summary;

    @Column(name = "voucher_no", length = 64)
    private String voucherNo;

    @Column(nullable = false)
    private Boolean matched = false;
}
