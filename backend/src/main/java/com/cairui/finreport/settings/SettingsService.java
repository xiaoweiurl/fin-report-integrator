package com.cairui.finreport.settings;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class SettingsService {
    public static final String CURRENT_PERIOD = "current_period";
    public static final String RECON_TOLERANCE_DAYS = "recon_tolerance_days";

    private final AppSettingRepository repo;

    public SettingsService(AppSettingRepository repo) {
        this.repo = repo;
    }

    public String get(String key, String fallback) {
        return repo.findById(Objects.requireNonNull(key)).map(s -> s.getValue()).orElse(fallback);
    }

    @Transactional
    public void put(String key, String value) {
        AppSetting s = repo.findById(Objects.requireNonNull(key)).orElseGet(() -> {
            AppSetting n = new AppSetting();
            n.setKey(key);
            return n;
        });
        s.setValue(value);
        repo.save(s);
    }

    public Map<String, String> all() {
        Map<String, String> map = new LinkedHashMap<>();
        repo.findAll().forEach(s -> map.put(s.getKey(), s.getValue()));
        return map;
    }
}
