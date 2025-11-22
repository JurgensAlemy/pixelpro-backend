package com.pixelpro.customers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "DTO para actualizar información básica del cliente")
public record CustomerUpdateDto(
        @Schema(description = "Correo electrónico", example = "juan.perez@email.com")
        @Email(message = "El formato del correo electrónico es inválido")
        @Size(max = 150, message = "El correo no puede tener más de 150 caracteres")
        String email,

        @Schema(description = "Número de teléfono", example = "987654321")
        @Size(min = 9, max = 9, message = "El número de teléfono debe tener 9 dígitos")
        String phoneNumber
) {
}

