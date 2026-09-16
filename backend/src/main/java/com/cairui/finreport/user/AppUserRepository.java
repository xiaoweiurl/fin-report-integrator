package com.cairui.finreport.user;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface AppUserRepository {
    Optional<AppUser> findByUsername(@Param("username") String username);

    long count();

    int insert(AppUser user);
}
