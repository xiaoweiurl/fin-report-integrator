package com.cairui.finreport.master;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AccountRepository {
    List<Account> findAll();

    List<Account> findAllByOrderByCodeAsc();

    Optional<Account> findById(@Param("id") Long id);

    Optional<Account> findByCode(@Param("code") String code);

    boolean existsByCode(@Param("code") String code);

    long count();

    int insert(Account account);

    int update(Account account);

    int delete(Account account);
}
