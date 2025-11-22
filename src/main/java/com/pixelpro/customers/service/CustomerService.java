package com.pixelpro.customers.service;

import com.pixelpro.customers.dto.CustomerDto;
import com.pixelpro.customers.dto.CustomerUpdateDto;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    Page<CustomerDto> getAllCustomers(String search, DocumentType documentType, CustomerType customerType, Pageable pageable);

    CustomerDto getCustomerById(Long id);

    CustomerDto updateCustomer(Long id, CustomerUpdateDto dto);
}

