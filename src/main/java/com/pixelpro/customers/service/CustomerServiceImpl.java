package com.pixelpro.customers.service;

import com.pixelpro.auth.repository.UserRepository;
import com.pixelpro.common.exception.ResourceNotFoundException;
import com.pixelpro.customers.dto.*;
import com.pixelpro.customers.entity.AddressEntity;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;
import com.pixelpro.customers.mapper.AddressMapper;
import com.pixelpro.customers.mapper.CustomerMapper;
import com.pixelpro.customers.repository.AddressRepository;
import com.pixelpro.customers.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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

    // ==================== STORE ENDPOINTS (MY ACCOUNT) ====================

    @Override
    @Transactional
    public CustomerDto createProfile(String email, CustomerProfileUpdateDto dto) {
        // 1. Buscar el usuario por email
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 2. Verificar si ya existe un perfil de cliente para este usuario
        if (customerRepository.findByEmail(email).isPresent()) {
            throw new com.pixelpro.common.exception.ConflictException("El perfil ya existe");
        }

        // 3. Crear nueva entidad de cliente
        CustomerEntity customer = CustomerEntity.builder()
                .firstName(dto.firstName())
                .lastName(dto.lastName())
                .email(email)
                .phoneNumber(dto.phoneNumber())
                .documentType(dto.documentType())
                .documentNumber(dto.documentNumber())
                .customerType(CustomerType.NATURAL) // Valor por defecto
                .userAccount(user) // Vincular con el usuario
                .build();

        // 4. Guardar y retornar DTO
        CustomerEntity saved = customerRepository.save(customer);
        return customerMapper.toDto(saved);
    }

    @Override
    public CustomerDto getMyProfile(String email) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));
        return customerMapper.toDto(customer);
    }

    @Override
    @Transactional
    public CustomerDto updateMyProfile(String email, CustomerProfileUpdateDto dto) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        customer.setFirstName(dto.firstName());
        customer.setLastName(dto.lastName());
        customer.setPhoneNumber(dto.phoneNumber());
        customer.setDocumentType(dto.documentType());
        customer.setDocumentNumber(dto.documentNumber());

        CustomerEntity updated = customerRepository.save(customer);
        return customerMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void changePassword(String email, String newPassword) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        if (customer.getUserAccount() == null) {
            throw new ResourceNotFoundException("El cliente no tiene una cuenta de usuario asociada");
        }

        customer.getUserAccount().setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(customer.getUserAccount());
    }

    @Override
    public List<AddressDto> getMyAddresses(String email) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        return customer.getAddresses().stream()
                .map(addressMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public AddressDto addAddress(String email, AddressCreateDto dto) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        AddressEntity address = addressMapper.toEntity(dto);
        address.setCustomer(customer);

        AddressEntity saved = addressRepository.save(address);
        return addressMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAddress(String email, Long addressId) {
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));

        AddressEntity address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Dirección no encontrada"));

        // Validar que la dirección pertenece al cliente
        if (!address.getCustomer().getId().equals(customer.getId())) {
            throw new ResourceNotFoundException("Dirección no encontrada o no pertenece al cliente");
        }

        addressRepository.delete(address);
    }
}

