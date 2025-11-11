package com.pixelpro.customers.service.impl;

import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import com.pixelpro.customers.dto.admin.CustomerAdminPatchRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminResponse;
import com.pixelpro.customers.dto.web.CustomerWebPatchRequest;
import com.pixelpro.customers.dto.web.CustomerWebRequest;
import com.pixelpro.customers.dto.web.CustomerWebResponse;
import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.mapper.CustomerMapper;
import com.pixelpro.customers.repository.CustomerRepository;
import com.pixelpro.customers.service.CustomerService;
// --- CAMBIOS AQUÍ ---
import com.pixelpro.auth.entity.UserEntity;
import com.pixelpro.auth.repository.UserRepository;
// --- FIN DE CAMBIOS ---
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository; // Esta línea ahora funciona
    private final CustomerMapper customerMapper;

    // WEB CONTEXT (public site)
    @Override
    public CustomerWebResponse registerCustomer(CustomerWebRequest request) {
// ... (el resto del archivo es idéntico y no necesita cambios) ...
// ... (omitido por brevedad) ...
// ... (el resto del archivo es idéntico y no necesita cambios) ...
        // Validaciones de unicidad
        if (customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }
        if (customerRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException("Document already in use: " + request.documentNumber());
        }

        CustomerEntity entity = customerMapper.toEntity(request);
        // Flujo web por defecto, tipo NATURAL
        if (entity.getCustomerType() == null) {
            entity.setCustomerType(CustomerType.NATURAL);
        }
        entity = customerRepository.save(entity);
        return customerMapper.toWebResponse(entity);
    }

    @Override
    public CustomerWebResponse getProfile(Long customerId) {
        CustomerEntity entity = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + customerId));
        return customerMapper.toWebResponse(entity);
    }

    @Override
    public CustomerWebResponse updateProfile(Long customerId, CustomerWebRequest request) {
        CustomerEntity entity = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + customerId));

        // Unicidad de email si cambió
        if (!entity.getEmail().equalsIgnoreCase(request.email())
                && customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }

        // Unicidad de documento si cambió
        if (!entity.getDocumentNumber().equalsIgnoreCase(request.documentNumber())
                && customerRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException("Document already in use: " + request.documentNumber());
        }

        // MapStruct: puedes crear un método update-from-request si prefieres (patcher).
        customerMapper.updateFromWebRequest(request, entity);

        return customerMapper.toWebResponse(entity);
    }

    @Override
    public CustomerWebResponse patchProfile(Long id, CustomerWebPatchRequest request) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
        customerMapper.updateFromWebPatch(request, entity);
        return customerMapper.toWebResponse(entity);
    }

    // ADMIN
    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CustomerAdminResponse> getAllCustomers(Pageable pageable) {
        Page<CustomerEntity> page = customerRepository.findAll(pageable);
        return customerMapper.toAdminResponseList(page.getContent());
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public CustomerAdminResponse getCustomerById(Long id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
        return customerMapper.toAdminResponse(entity);
    }

    @Override
    public CustomerAdminResponse createCustomer(CustomerAdminRequest request) {
        if (customerRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already in use: " + request.email());
        }
        if (customerRepository.existsByDocumentNumber(request.documentNumber())) {
            throw new ConflictException("Document already in use: " + request.documentNumber());
        }

        CustomerEntity entity = customerMapper.toEntity(request);
        entity = customerRepository.save(entity);
        return customerMapper.toAdminResponse(entity);
    }

    @Override
    public CustomerAdminResponse updateCustomer(Long id, CustomerAdminRequest request) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));

        // Validar unicidad excluyendo este id
        if (customerRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("Email already in new use: " + request.email());
        }
        if (customerRepository.existsByDocumentNumberAndIdNot(request.documentNumber(), id)) {
            throw new ConflictException("Document already in use: " + request.documentNumber());
        }

        customerMapper.updateFromAdminRequest(request, entity);
        return customerMapper.toAdminResponse(entity);
    }

    @Override
    public CustomerAdminResponse patchCustomer(Long id, CustomerAdminPatchRequest request) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
        customerMapper.updateFromAdminPatch(request, entity);
        return customerMapper.toAdminResponse(entity);
    }

    @Override
    public void deleteCustomer(Long id) {
        CustomerEntity entity = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: id=" + id));
        customerRepository.delete(entity);
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<CustomerAdminResponse> searchCustomers(String query) {
        // Elegimos una paginación “sana” si luego quieres sobrecargar con Pageable en el controller.
        Page<CustomerEntity> page = customerRepository.search(query == null ? "" : query.trim(), Pageable.ofSize(20));
        return customerMapper.toAdminResponseList(page.getContent());
    }

    public CustomerWebResponse createOrUpdateProfile(Long userId, CustomerWebRequest request) {
        // 1. Buscar usuario
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: id=" + userId));

        // 2. Intentar encontrar perfil existente
        CustomerEntity customer = customerRepository.findByUserAccount(user)
                .orElse(null);

        if (customer == null) {
            // Crear nuevo perfil
            customer = customerMapper.toEntity(request);
            customer.setUserAccount(user);
            customer.setEmail(user.getEmail());
        } else {
            // Actualizar datos existentes
            customerMapper.updateFromWebRequest(request, customer);
        }

        // 3. Guardar y devolver
        customer = customerRepository.save(customer);
        return customerMapper.toWebResponse(customer);
    }
}