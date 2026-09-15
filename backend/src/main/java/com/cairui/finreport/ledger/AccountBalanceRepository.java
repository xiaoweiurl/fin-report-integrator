package com.cairui.finreport.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface AccountBalanceRepository extends JpaRepository<AccountBalance, Long> {
    List<AccountBalance> findByPeriodOrderByAccountCodeAsc(String period);

    long countByPeriod(String period);

    @Query("select sum(b.closingDebit) from AccountBalance b where b.period = :period")
    BigDecimal sumClosingDebitByPeriod(@Param("period") String period);

    @Query("select sum(b.closingCredit) from AccountBalance b where b.period = :period")
    BigDecimal sumClosingCreditByPeriod(@Param("period") String period);

    @Modifying
    void deleteByPeriod(String period);
}
