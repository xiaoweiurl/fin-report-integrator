package com.cairui.finreport.master;

import com.cairui.finreport.audit.AuditService;
import com.cairui.finreport.common.ApiException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {
    private final PartnerRepository repo;
    private final AuditService audit;

    public PartnerController(PartnerRepository repo, AuditService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    @GetMapping
    public List<Partner> list() {
        return repo.findAllByOrderByCodeAsc();
    }

    @PostMapping
    public Partner create(@Valid @RequestBody PartnerReq req, Authentication auth) {
        if (repo.findByCode(req.code()).isPresent()) {
            throw ApiException.badRequest("往来编码已存在");
        }
        Partner p = toEntity(new Partner(), req);
        Partner saved = repo.save(p);
        audit.log("PARTNER_CREATE", "PARTNER", saved.getCode(), saved.getName(), auth.getName());
        return saved;
    }

    @PutMapping("/{id}")
    public Partner update(@PathVariable Long id, @Valid @RequestBody PartnerReq req, Authentication auth) {
        Partner p = repo.findById(id).orElseThrow(() -> ApiException.notFound("往来单位不存在"));
        toEntity(p, req);
        Partner saved = repo.save(p);
        audit.log("PARTNER_UPDATE", "PARTNER", saved.getCode(), saved.getName(), auth.getName());
        return saved;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Partner p = repo.findById(id).orElseThrow(() -> ApiException.notFound("往来单位不存在"));
        repo.delete(p);
        audit.log("PARTNER_DELETE", "PARTNER", p.getCode(), p.getName(), auth.getName());
    }

    private Partner toEntity(Partner p, PartnerReq req) {
        p.setCode(req.code().trim());
        p.setName(req.name().trim());
        p.setPartnerType(req.partnerType());
        p.setEnabled(req.enabled() == null || req.enabled());
        return p;
    }

    public record PartnerReq(
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String partnerType,
            Boolean enabled
    ) {
    }
}
