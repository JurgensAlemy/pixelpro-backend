package com.pixelpro.customers.dto.web;

import com.pixelpro.customers.entity.enums.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CustomerWebRequest(
        @NotBlank @Size(max = 60) String firstName,
        @NotBlank @Size(max = 60) String lastName,
        @Email @NotBlank @Size(max = 150) String email,
        @NotBlank @Size(max = 9) String phoneNumber,
        @NotNull DocumentType documentType,
        @NotBlank @Size(max = 15) String documentNumber
) {
}
