package com.pixelpro.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información del cliente")
public record CustomerDto(
        @Schema(description = "ID del cliente", example = "1")
        Long id,

        @Schema(description = "Nombre del cliente", example = "Juan")
        String firstName,

        @Schema(description = "Apellido del cliente", example = "Pérez")
        String lastName,

        @Schema(description = "Correo electrónico", example = "juan.perez@email.com")
        String email,

        @Schema(description = "Número de teléfono", example = "987654321")
        String phoneNumber,

        @Schema(description = "Tipo de documento", example = "DNI")
        String documentType,

        @Schema(description = "Número de documento", example = "12345678")
        String documentNumber
) {
}

