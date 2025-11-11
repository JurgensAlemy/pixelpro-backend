package com.pixelpro.customers.dto.web;

import com.pixelpro.customers.entity.enums.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CustomerWebPatchRequest(
        @Size(max = 60) String firstName,
        @Size(max = 60) String lastName,
        @Email @Size(max = 150) String email,
        @Size(max = 9) String phoneNumber,
        DocumentType documentType,
        @Size(max = 15) String documentNumber
) {
}
