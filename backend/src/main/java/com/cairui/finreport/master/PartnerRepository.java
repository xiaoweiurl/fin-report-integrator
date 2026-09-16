package com.cairui.finreport.master;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PartnerRepository {
    List<Partner> findAllByOrderByCodeAsc();

    Optional<Partner> findById(@Param("id") Long id);

    Optional<Partner> findByCode(@Param("code") String code);

    long count();

    int insert(Partner partner);

    int update(Partner partner);

    int delete(Partner partner);
}
