package com.cairui.finreport.user;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AppUser {
    private Long id;
    private String username;
    private String passwordHash;
    private String displayName;
    private String role;
    private LocalDateTime createdAt;
}
