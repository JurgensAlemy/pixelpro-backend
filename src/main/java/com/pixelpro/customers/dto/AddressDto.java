package com.pixelpro.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dirección de envío")
public record AddressDto(
        @Schema(description = "ID de la dirección", example = "1")
        Long id,

        @Schema(description = "Tipo de dirección", example = "DELIVERY")
        String addressType,

        @Schema(description = "Departamento", example = "Lima")
        String department,

        @Schema(description = "Provincia", example = "Lima")
        String province,

        @Schema(description = "Distrito", example = "Miraflores")
        String district,

        @Schema(description = "Dirección completa", example = "Av. Larco 1234")
        String addressLine,

        @Schema(description = "Referencia de ubicación", example = "Al frente del parque")
        String addressReference,

        @Schema(description = "Teléfono de contacto", example = "987654321")
        String addressPhone
) {
}

