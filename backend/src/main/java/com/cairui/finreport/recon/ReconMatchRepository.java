package com.cairui.finreport.recon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface ReconMatchRepository extends JpaRepository<ReconMatch, Long> {
    List<ReconMatch> findByPeriodOrderByIdAsc(String period);

    @Modifying
    void deleteByPeriod(String period);
}
