package com.cairui.finreport.recon;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recon")
public class ReconController {
    private final ReconService recon;

    public ReconController(ReconService recon) {
        this.recon = recon;
    }

    @GetMapping
    public ReconService.ReconView view(@RequestParam String period) {
        return recon.view(period);
    }

    @PostMapping("/run")
    public ReconService.ReconView run(
            @RequestParam String period,
            @RequestParam(required = false) Integer toleranceDays,
            Authentication auth) {
        return recon.run(period, toleranceDays, auth.getName());
    }
}
