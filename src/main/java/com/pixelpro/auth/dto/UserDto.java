package com.pixelpro.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información del usuario del sistema")
public record UserDto(
        @Schema(description = "ID único del usuario", example = "1")
        Long id,

        @Schema(description = "Correo electrónico del usuario", example = "admin@pixelpro.com")
        String email,

        @Schema(description = "Nombre del rol asignado", example = "ADMIN")
        String roleName,

        @Schema(description = "Estado de activación del usuario", example = "true")
        boolean enabled,

        @Schema(description = "Fecha de creación del usuario")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización")
        LocalDateTime updatedAt
) {
}

