package com.pixelpro.customers.repository;

import com.pixelpro.customers.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {
    List<AddressEntity> findByCustomer_Id(Long customerId);

    Optional<AddressEntity> findByIdAndCustomer_Id(Long id, Long customerId);
}
