package com.pixelpro.customers.service;

import com.pixelpro.common.exception.ResourceNotFoundException;
import com.pixelpro.customers.dto.CustomerDto;
import com.pixelpro.customers.dto.CustomerUpdateDto;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;
import com.pixelpro.customers.mapper.CustomerMapper;
import com.pixelpro.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public Page<CustomerDto> getAllCustomers(String search, DocumentType documentType, CustomerType customerType, Pageable pageable) {
        Page<CustomerEntity> customers = customerRepository.findByFilters(search, documentType, customerType, pageable);
        return customers.map(customerMapper::toDto);
    }

    @Override
    public CustomerDto getCustomerById(Long id) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerDto updateCustomer(Long id, CustomerUpdateDto dto) {
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));

        customerMapper.updateEntityFromDto(dto, customer);
        CustomerEntity updated = customerRepository.save(customer);
        return customerMapper.toDto(updated);
    }
}

