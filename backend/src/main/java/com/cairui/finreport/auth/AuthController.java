package com.cairui.finreport.auth;

import com.cairui.finreport.audit.AuditService;
import com.cairui.finreport.common.ApiException;
import com.cairui.finreport.user.AppUser;
import com.cairui.finreport.user.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final AuditService auditService;

    public AuthController(AppUserRepository users, PasswordEncoder encoder, JwtService jwtService, AuditService auditService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        AppUser user = users.findByUsername(req.username())
                .orElseThrow(() -> ApiException.unauthorized("用户名或密码错误"));
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("用户名或密码错误");
        }
        String token = jwtService.issue(user.getUsername());
        auditService.log("LOGIN", "USER", String.valueOf(user.getId()), "登录成功", user.getUsername());
        return new LoginResponse(token, user.getUsername(), user.getDisplayName(), user.getRole());
    }

    @GetMapping("/me")
    public LoginResponse me(Authentication auth) {
        AppUser user = users.findByUsername(auth.getName())
                .orElseThrow(() -> ApiException.unauthorized("未登录"));
        return new LoginResponse(null, user.getUsername(), user.getDisplayName(), user.getRole());
    }
}
