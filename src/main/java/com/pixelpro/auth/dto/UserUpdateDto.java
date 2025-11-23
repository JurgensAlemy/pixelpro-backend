package com.pixelpro.auth.dto;

import com.pixelpro.auth.entity.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para actualizar información de un usuario existente")
public record UserUpdateDto(
        @Schema(description = "Correo electrónico del usuario", example = "usuario@pixelpro.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es inválido")
        @Size(max = 150, message = "El email no puede exceder los 150 caracteres")
        String email,

        @Schema(description = "Rol del usuario", example = "CLIENTE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"ADMIN", "CLIENTE"})
        @NotNull(message = "El rol es obligatorio")
        RoleEnum role,

        @Schema(description = "Estado de activación del usuario", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El estado es obligatorio")
        Boolean enabled
) {
}

