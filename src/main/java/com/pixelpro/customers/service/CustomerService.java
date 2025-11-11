package com.pixelpro.customers.service;

import com.pixelpro.customers.dto.admin.CustomerAdminPatchRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminRequest;
import com.pixelpro.customers.dto.admin.CustomerAdminResponse;
import com.pixelpro.customers.dto.web.CustomerWebPatchRequest;
import com.pixelpro.customers.dto.web.CustomerWebRequest;
import com.pixelpro.customers.dto.web.CustomerWebResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {
    // WEB CONTEXT (public site)

    /**
     * Register a new customer (either guest or account-based).
     */
    CustomerWebResponse registerCustomer(CustomerWebRequest request);

    /**
     * Get the profile of a customer (usually authenticated).
     */
    CustomerWebResponse getProfile(Long customerId);

    /**
     * Update basic customer information from the public site.
     */
    CustomerWebResponse updateProfile(Long customerId, CustomerWebRequest request);

    CustomerWebResponse patchProfile(Long id, CustomerWebPatchRequest request);

    CustomerWebResponse createOrUpdateProfile(Long userId, CustomerWebRequest request);
    // ADMIN CONTEXT (CMS panel)

    /**
     * Get all customers, optionally paginated.
     */
    List<CustomerAdminResponse> getAllCustomers(Pageable pageable);

    /**
     * Retrieve detailed information of a specific customer.
     */
    CustomerAdminResponse getCustomerById(Long id);

    /**
     * Create a new customer manually from the admin panel.
     */
    CustomerAdminResponse createCustomer(CustomerAdminRequest request);

    /**
     * Update customer details from the admin panel.
     */
    CustomerAdminResponse updateCustomer(Long id, CustomerAdminRequest request);

    CustomerAdminResponse patchCustomer(Long id, CustomerAdminPatchRequest request);

    /**
     * Delete a customer (preferably soft delete if implemented).
     */
    void deleteCustomer(Long id);

    /**
     * Search customers by name, email, or document number.
     */
    List<CustomerAdminResponse> searchCustomers(String query);
}
