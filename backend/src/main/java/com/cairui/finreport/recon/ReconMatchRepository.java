package com.cairui.finreport.recon;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReconMatchRepository {
    List<ReconMatch> findByPeriodOrderByIdAsc(@Param("period") String period);

    long countByPeriod(@Param("period") String period);

    int deleteByPeriod(@Param("period") String period);

    int insert(ReconMatch match);
}
