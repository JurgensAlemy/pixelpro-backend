package com.pixelpro.catalog.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryAdminUpdateRequest(
        String name,
        Long parentId
) {
}
