package com.pixelpro.auth.dto;

import java.util.Set;

public record AuthResponse(
        Long id,
        String email,
        Set<String> roles,
        boolean authenticated
) {
}
