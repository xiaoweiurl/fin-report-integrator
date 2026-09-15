package com.cairui.finreport.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {
    List<AccountBalance> findByPeriodOrderByAccountCodeAsc(String period);

    @Modifying
    void deleteByPeriod(String period);
}
