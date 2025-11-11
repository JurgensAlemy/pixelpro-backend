package com.pixelpro.catalog.dto.web;

import java.util.List;

public record ProductWebResponse(
        Long id,
        String name,
        String description,
        List<String> categoryNames,
        List<ProductImageWebResponse> images
) {
}
