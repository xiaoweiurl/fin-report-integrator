package com.cairui.finreport.settings;

import com.cairui.finreport.audit.AuditService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public settings API (period / recon tolerance); current UI stores period locally. */
@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService settings;
    private final AuditService audit;

    public SettingsController(SettingsService settings, AuditService audit) {
        this.settings = settings;
        this.audit = audit;
    }

    @GetMapping
    public Map<String, String> get() {
        return settings.all();
    }

    @PutMapping
    public Map<String, String> update(@RequestBody Map<String, String> body, Authentication auth) {
        body.forEach(settings::put);
        audit.log("SETTINGS_UPDATE", "SETTINGS", null, body.toString(), auth.getName());
        return settings.all();
    }
}
