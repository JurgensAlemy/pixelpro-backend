package com.pixelpro.customers.controller;

import com.pixelpro.customers.dto.shared.AddressDto;
import com.pixelpro.customers.dto.web.CustomerWebPatchRequest;
import com.pixelpro.customers.dto.web.CustomerWebRequest;
import com.pixelpro.customers.dto.web.CustomerWebResponse;
import com.pixelpro.customers.service.AddressService;
import com.pixelpro.customers.service.CustomerService;
// --- CAMBIOS AQUÍ ---
// Ya no importamos el UserService de 'iam'
import com.pixelpro.auth.entity.UserEntity; // Importamos la entidad de 'auth'
import com.pixelpro.auth.repository.UserRepository; // Importamos el repositorio de 'auth'
// --- FIN DE CAMBIOS ---
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/customers")
@RequiredArgsConstructor
@Tag(name = "Public - Customers", description = "Endpoints for customer actions on the public website")
public class PublicCustomerController {
    private final CustomerService customerService;
    private final AddressService addressService;

    // --- CAMBIO AQUÍ ---
    // Cambiamos UserService por UserRepository
    private final UserRepository userRepository;
    // --- FIN DE CAMBIO ---


    // CUSTOMER PROFILE
    @Operation(summary = "Register a new customer (checkout or signup)")
    @PostMapping
    public ResponseEntity<CustomerWebResponse> register(
            @Valid @RequestBody CustomerWebRequest request
    ) {
        var created = customerService.registerCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ... (Todos los demás métodos de 'getProfile', 'updateProfile', 'patchProfile' y 'Addresses' quedan igual) ...
    // ... (Omitidos por brevedad) ...

    @Operation(summary = "Get customer profile by ID")
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerWebResponse> getProfile(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getProfile(customerId));
    }

    @Operation(summary = "Update customer profile")
    @PutMapping("/{customerId}")
    public ResponseEntity<CustomerWebResponse> updateProfile(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerWebRequest request
    ) {
        return ResponseEntity.ok(customerService.updateProfile(customerId, request));
    }

    @Operation(summary = "Partially update customer profile")
    @PatchMapping("/{customerId}")
    public ResponseEntity<CustomerWebResponse> patchProfile(
            @PathVariable Long customerId,
            @Valid @RequestBody CustomerWebPatchRequest request
    ) {
        return ResponseEntity.ok(customerService.patchProfile(customerId, request));
    }

    // ADDRESSES
    @Operation(summary = "Add new address to a customer")
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressDto> addAddress(
            @PathVariable Long customerId,
            @Valid @RequestBody AddressDto dto
    ) {
        var created = addressService.addAddress(customerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "List all addresses of a customer")
    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<AddressDto>> getAddresses(@PathVariable Long customerId) {
        return ResponseEntity.ok(addressService.getAddresses(customerId));
    }

    @Operation(summary = "Update an existing address of a customer")
    @PutMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<AddressDto> updateAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressDto dto
    ) {
        return ResponseEntity.ok(addressService.updateAddress(customerId, addressId, dto));
    }

    @Operation(summary = "Delete an address of a customer")
    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId
    ) {
        addressService.deleteAddress(customerId, addressId);
        return ResponseEntity.noContent().build();
    }


    @Operation(summary = "Create or update customer profile (for logged-in users)")
    @PostMapping("/profile")
    public ResponseEntity<CustomerWebResponse> createOrUpdateProfile(
            @Valid @RequestBody CustomerWebRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // --- CAMBIO AQUÍ ---
        // Buscamos en el 'userRepository' en lugar de 'userService'
        UserEntity user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        // --- FIN DE CAMBIO ---

        var response = customerService.createOrUpdateProfile(user.getId(), request);
        return ResponseEntity.ok(response);
    }
}