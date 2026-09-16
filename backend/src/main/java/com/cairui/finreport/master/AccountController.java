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
import java.util.Objects;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountRepository repo;
    private final AuditService audit;

    public AccountController(AccountRepository repo, AuditService audit) {
        this.repo = repo;
        this.audit = audit;
    }

    @GetMapping
    public List<Account> list() {
        return repo.findAllByOrderByCodeAsc();
    }

    @PostMapping
    public Account create(@Valid @RequestBody AccountReq req, Authentication auth) {
        if (repo.existsByCode(req.code())) {
            throw ApiException.badRequest("科目编码已存在: " + req.code());
        }
        Account a = toEntity(new Account(), req);
        repo.insert(a);
        audit.log("ACCOUNT_CREATE", "ACCOUNT", a.getCode(), a.getName(), auth.getName());
        return a;
    }

    @PutMapping("/{id}")
    public Account update(@PathVariable Long id, @Valid @RequestBody AccountReq req, Authentication auth) {
        Account a = repo.findById(Objects.requireNonNull(id)).orElseThrow(() -> ApiException.notFound("科目不存在"));
        toEntity(a, req);
        repo.update(a);
        audit.log("ACCOUNT_UPDATE", "ACCOUNT", a.getCode(), a.getName(), auth.getName());
        return a;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, Authentication auth) {
        Account a = repo.findById(Objects.requireNonNull(id)).orElseThrow(() -> ApiException.notFound("科目不存在"));
        repo.delete(Objects.requireNonNull(a));
        audit.log("ACCOUNT_DELETE", "ACCOUNT", a.getCode(), a.getName(), auth.getName());
    }

    private Account toEntity(Account a, AccountReq req) {
        a.setCode(req.code().trim());
        a.setName(req.name().trim());
        a.setCategory(req.category());
        a.setBalanceSide(req.balanceSide());
        a.setParentCode(req.parentCode());
        a.setLevelNo(req.levelNo() == null ? 1 : req.levelNo());
        a.setEnabled(req.enabled() == null || req.enabled());
        return a;
    }

    public record AccountReq(
            @NotBlank String code,
            @NotBlank String name,
            @NotBlank String category,
            @NotBlank String balanceSide,
            String parentCode,
            Integer levelNo,
            Boolean enabled
    ) {
    }
}
