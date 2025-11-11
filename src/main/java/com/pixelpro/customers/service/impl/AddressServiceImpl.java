package com.pixelpro.customers.service.impl;

import com.pixelpro.common.exception.ResourceNotFoundException;
import com.pixelpro.customers.dto.shared.AddressDto;
import com.pixelpro.customers.entity.AddressEntity;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.mapper.AddressMapper;
import com.pixelpro.customers.repository.AddressRepository;
import com.pixelpro.customers.repository.CustomerRepository;
import com.pixelpro.customers.service.AddressService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AddressServiceImpl implements AddressService {
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    // WEB CONTEXT (public site)
    @Override
    public AddressDto addAddress(Long customerId, AddressDto dto) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + customerId));

        AddressEntity entity = addressMapper.toEntity(dto);
        entity.setCustomer(customer);

        AddressEntity saved = addressRepository.save(entity);
        return addressMapper.toDto(saved);
    }

    @Override
    public List<AddressDto> getAddresses(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: id=" + customerId);
        }
        return addressMapper.toDtoList(addressRepository.findByCustomer_Id(customerId));
    }

    @Override
    public AddressDto updateAddress(Long customerId, Long addressId, AddressDto dto) {
        AddressEntity entity = addressRepository.findByIdAndCustomer_Id(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found for customerId=" + customerId + ", addressId=" + addressId));

        addressMapper.updateEntityFromDto(dto, entity);
        return addressMapper.toDto(entity);
    }

    @Override
    public void deleteAddress(Long customerId, Long addressId) {
        AddressEntity entity = addressRepository.findByIdAndCustomer_Id(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found for customerId=" + customerId + ", addressId=" + addressId));
        addressRepository.delete(entity);
    }

    // ADMIN
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<AddressDto> getAddressesByCustomerId(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found: id=" + customerId);
        }
        return addressMapper.toDtoList(addressRepository.findByCustomer_Id(customerId));
    }

    @Override
    public AddressDto addAddressByAdmin(Long customerId, AddressDto dto) {
        CustomerEntity customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + customerId));

        AddressEntity entity = addressMapper.toEntity(dto);
        entity.setCustomer(customer);
        AddressEntity saved = addressRepository.save(entity);
        return addressMapper.toDto(saved);
    }

    @Override
    public void deleteAddressByAdmin(Long customerId, Long addressId) {
        AddressEntity entity = addressRepository.findByIdAndCustomer_Id(addressId, customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Address not found for customerId=" + customerId + ", addressId=" + addressId));
        addressRepository.delete(entity);
    }
}
