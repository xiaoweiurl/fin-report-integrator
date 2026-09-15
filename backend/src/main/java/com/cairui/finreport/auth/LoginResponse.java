package com.cairui.finreport.auth;

public record LoginResponse(String token, String username, String displayName, String role) {
}
