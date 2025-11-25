package com.pixelpro.customers.dto;

import com.pixelpro.customers.entity.enums.DocumentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para actualizar el perfil del cliente")
public record CustomerProfileUpdateDto(
        @Schema(description = "Nombres del cliente", example = "Juan Carlos", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 60, message = "El nombre no puede exceder los 60 caracteres")
        String firstName,

        @Schema(description = "Apellidos del cliente", example = "Pérez García", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 60, message = "El apellido no puede exceder los 60 caracteres")
        String lastName,

        @Schema(description = "Número de teléfono", example = "987654321", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 9, message = "El teléfono debe tener 9 dígitos")
        String phoneNumber,

        @Schema(description = "Tipo de documento", example = "DNI", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El tipo de documento es obligatorio")
        DocumentType documentType,

        @Schema(description = "Número de documento", example = "12345678", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El número de documento es obligatorio")
        @Size(max = 15, message = "El número de documento no puede exceder los 15 caracteres")
        String documentNumber
) {
}

