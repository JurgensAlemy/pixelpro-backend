package com.pixelpro.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para crear o actualizar una dirección")
public record AddressCreateDto(
        @Schema(description = "Tipo de dirección", example = "Casa", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El tipo de dirección es obligatorio")
        String addressType,

        @Schema(description = "Departamento", example = "Lima", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El departamento es obligatorio")
        @Size(max = 100)
        String department,

        @Schema(description = "Provincia", example = "Lima", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La provincia es obligatoria")
        @Size(max = 100)
        String province,

        @Schema(description = "Distrito", example = "Miraflores", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El distrito es obligatorio")
        @Size(max = 100)
        String district,

        @Schema(description = "Dirección completa", example = "Av. Larco 1234", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255)
        String addressLine,

        @Schema(description = "Referencia de ubicación", example = "Al frente del parque")
        @Size(max = 255)
        String addressReference,

        @Schema(description = "Teléfono de contacto", example = "987654321", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 9)
        String addressPhone
) {
}

