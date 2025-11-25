package com.pixelpro.customers.service;

import com.pixelpro.customers.dto.*;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    // Admin endpoints
    Page<CustomerDto> getAllCustomers(String search, DocumentType documentType, CustomerType customerType, Pageable pageable);

    CustomerDto getCustomerById(Long id);

    CustomerDto updateCustomer(Long id, CustomerUpdateDto dto);

    // Store endpoints (My Account)
    CustomerDto createProfile(String email, CustomerProfileUpdateDto dto);

    CustomerDto getMyProfile(String email);

    CustomerDto updateMyProfile(String email, CustomerProfileUpdateDto dto);

    void changePassword(String email, String newPassword);

    List<AddressDto> getMyAddresses(String email);

    AddressDto addAddress(String email, AddressCreateDto dto);

    void deleteAddress(String email, Long addressId);
}

