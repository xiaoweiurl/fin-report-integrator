package com.cairui.finreport.ledger;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface AccountBalanceRepository {
    List<AccountBalance> findByPeriodOrderByAccountCodeAsc(@Param("period") String period);

    long countByPeriod(@Param("period") String period);

    BigDecimal sumClosingDebitByPeriod(@Param("period") String period);

    BigDecimal sumClosingCreditByPeriod(@Param("period") String period);

    int deleteByPeriod(@Param("period") String period);

    int insert(AccountBalance row);

    int insertBatch(@Param("list") List<AccountBalance> list);
}
