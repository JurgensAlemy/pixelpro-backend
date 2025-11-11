package com.pixelpro.customers.controller;

import com.pixelpro.customers.dto.admin.CustomerAdminPatchRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminResponse;
import com.pixelpro.customers.dto.shared.AddressDto;
import com.pixelpro.customers.service.AddressService;
import com.pixelpro.customers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Admin - Customers", description = "Endpoints for managing customers from the admin panel")
public class AdminCustomerController {
    private final CustomerService customerService;
    private final AddressService addressService;

    // CUSTOMERS
    @Operation(summary = "List all customers (paginated)")
    @GetMapping
    public ResponseEntity<List<CustomerAdminResponse>> getAll(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @Operation(summary = "Get customer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerAdminResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @Operation(summary = "Search customers by name, email or document")
    @GetMapping("/search")
    public ResponseEntity<List<CustomerAdminResponse>> search(
            @RequestParam String query
    ) {
        return ResponseEntity.ok(customerService.searchCustomers(query));
    }

    @Operation(summary = "Create a new customer")
    @PostMapping
    public ResponseEntity<CustomerAdminResponse> create(
            @Valid @RequestBody CustomerAdminRequest request
    ) {
        var created = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update customer information")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerAdminResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerAdminRequest request
    ) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @Operation(summary = "Partially update customer data (admin)")
    @PatchMapping("/{id}")
    public ResponseEntity<CustomerAdminResponse> patchCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerAdminPatchRequest request
    ) {
        return ResponseEntity.ok(customerService.patchCustomer(id, request));
    }

    @Operation(summary = "Delete a customer")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    // ADDRESSES (admin view)
    @Operation(summary = "List addresses of a customer")
    @GetMapping("/{customerId}/addresses")
    public ResponseEntity<List<AddressDto>> getAddresses(@PathVariable Long customerId) {
        return ResponseEntity.ok(addressService.getAddressesByCustomerId(customerId));
    }

    @Operation(summary = "Add address to a customer")
    @PostMapping("/{customerId}/addresses")
    public ResponseEntity<AddressDto> addAddress(
            @PathVariable Long customerId,
            @Valid @RequestBody AddressDto dto
    ) {
        var created = addressService.addAddressByAdmin(customerId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Delete an address from a customer")
    @DeleteMapping("/{customerId}/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long customerId,
            @PathVariable Long addressId
    ) {
        addressService.deleteAddressByAdmin(customerId, addressId);
        return ResponseEntity.noContent().build();
    }
}
