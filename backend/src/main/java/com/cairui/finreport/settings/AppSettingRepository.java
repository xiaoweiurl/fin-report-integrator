package com.cairui.finreport.settings;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface AppSettingRepository {
    Optional<AppSetting> findById(@Param("key") String key);

    List<AppSetting> findAll();

    int insert(AppSetting setting);

    int update(AppSetting setting);
}
