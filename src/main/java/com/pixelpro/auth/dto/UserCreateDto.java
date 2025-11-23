package com.pixelpro.auth.dto;

import com.pixelpro.auth.entity.RoleEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para crear un nuevo usuario en el sistema")
public record UserCreateDto(
        @Schema(description = "Correo electrónico del usuario", example = "usuario@pixelpro.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email es inválido")
        @Size(max = 150, message = "El email no puede exceder los 150 caracteres")
        String email,

        @Schema(description = "Contraseña del usuario", example = "Password123!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @Schema(description = "Rol del usuario", example = "CLIENTE", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"ADMIN", "CLIENTE"})
        @NotNull(message = "El rol es obligatorio")
        RoleEnum role
) {
}

