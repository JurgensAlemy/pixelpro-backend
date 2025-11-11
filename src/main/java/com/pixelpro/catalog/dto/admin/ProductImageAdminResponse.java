package com.pixelpro.catalog.dto.admin;

public record ProductImageAdminResponse(
        Long id,
        String url,
        Byte position
) {
}
