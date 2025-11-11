package com.pixelpro.customers.dto.shared;

public record AddressDto(
        Long id,
        String addressType,
        String department,
        String province,
        String district,
        String addressLine,
        String addressReference,
        String addressPhone
) {
}
