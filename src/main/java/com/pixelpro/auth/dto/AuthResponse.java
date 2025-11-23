package com.pixelpro.auth.dto;

public record AuthResponse(
        Long id,
        String email,
        String rol,
        boolean authenticated
) {
}
