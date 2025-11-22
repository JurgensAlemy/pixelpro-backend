package com.pixelpro.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

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
        String documentNumber,

        @Schema(description = "Tipo de cliente", example = "NATURAL")
        String customerType,

        @Schema(description = "Fecha de creación", example = "2024-01-15T10:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha de última actualización", example = "2024-01-15T10:30:00")
        LocalDateTime updatedAt,

        @Schema(description = "Lista de direcciones del cliente")
        List<AddressDto> addresses
) {
}

