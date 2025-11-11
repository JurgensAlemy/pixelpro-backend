package com.pixelpro.customers.dto.admin;

import com.pixelpro.customers.dto.shared.AddressDto;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;

import java.time.LocalDateTime;
import java.util.List;

public record CustomerAdminResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        DocumentType documentType,
        String documentNumber,
        CustomerType customerType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<AddressDto> addresses
) {
}
