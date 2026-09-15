package com.cairui.finreport.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface CashJournalRepository extends JpaRepository<CashJournalLine, Long> {
    List<CashJournalLine> findByPeriodOrderByTxnDateAscIdAsc(String period);

    @Modifying
    void deleteByPeriod(String period);
}
