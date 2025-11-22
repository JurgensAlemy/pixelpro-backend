package com.pixelpro.customers.repository;

import com.pixelpro.customers.entity.CustomerEntity;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    @Query("""
            SELECT c FROM CustomerEntity c
            WHERE (
                :search IS NULL OR :search = '' OR
                LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR
                LOWER(c.documentNumber) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            AND (:documentType IS NULL OR c.documentType = :documentType)
            AND (:customerType IS NULL OR c.customerType = :customerType)
            """)
    Page<CustomerEntity> findByFilters(
            @Param("search") String search,
            @Param("documentType") DocumentType documentType,
            @Param("customerType") CustomerType customerType,
            Pageable pageable
    );
}