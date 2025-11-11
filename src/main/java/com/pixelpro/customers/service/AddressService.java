package com.pixelpro.customers.service;

import com.pixelpro.customers.dto.shared.AddressDto;

import java.util.List;

public interface AddressService {
    // =============================
    // 🌐 WEB CONTEXT (public site)
    // =============================

    /**
     * Add a new address for a specific customer.
     */
    AddressDto addAddress(Long customerId, AddressDto dto);

    /**
     * Retrieve all addresses of a specific customer.
     */
    List<AddressDto> getAddresses(Long customerId);

    /**
     * Update an existing address of a customer.
     */
    AddressDto updateAddress(Long customerId, Long addressId, AddressDto dto);

    /**
     * Delete a customer's address by ID.
     */
    void deleteAddress(Long customerId, Long addressId);

    // =============================
    // 🧩 ADMIN CONTEXT (CMS panel)
    // =============================

    /**
     * Get all addresses belonging to a specific customer (admin view).
     */
    List<AddressDto> getAddressesByCustomerId(Long customerId);

    /**
     * Add an address to a customer from the admin panel.
     */
    AddressDto addAddressByAdmin(Long customerId, AddressDto dto);

    /**
     * Delete a customer's address from the admin panel.
     */
    void deleteAddressByAdmin(Long customerId, Long addressId);
}
