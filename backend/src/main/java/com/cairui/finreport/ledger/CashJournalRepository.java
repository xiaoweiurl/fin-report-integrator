package com.cairui.finreport.ledger;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CashJournalRepository {
    List<CashJournalLine> findByPeriodOrderByTxnDateAscIdAsc(@Param("period") String period);

    long countByPeriod(@Param("period") String period);

    int deleteByPeriod(@Param("period") String period);

    int insertBatch(@Param("list") List<CashJournalLine> list);

    int update(CashJournalLine line);
}
