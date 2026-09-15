package com.cairui.finreport.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface BankStatementRepository extends JpaRepository<BankStatementLine, Long> {
    List<BankStatementLine> findByPeriodOrderByTxnDateAscIdAsc(String period);

    @Modifying
    void deleteByPeriod(String period);
}
