package com.pixelpro.customers.dto.admin;

import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerAdminPatchRequest(
        @Size(max = 60) String firstName,
        @Size(max = 60) String lastName,
        @NotBlank @Size(max = 150) String email,
        @Size(max = 9) String phoneNumber,
        DocumentType documentType,
        @Size(max = 15) String documentNumber,
        CustomerType customerType
) {
}
