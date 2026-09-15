package com.cairui.finreport.master;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(String code);
    List<Account> findAllByOrderByCodeAsc();
    boolean existsByCode(String code);
}
