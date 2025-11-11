package com.pixelpro.customers.dto.web;

import com.pixelpro.customers.dto.shared.AddressDto;

import java.util.List;

public record CustomerWebResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        List<AddressDto> addresses
) {
}
