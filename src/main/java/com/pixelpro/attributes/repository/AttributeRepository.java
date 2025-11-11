package com.pixelpro.attributes.repository;

import com.pixelpro.attributes.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {
    Optional<AttributeEntity> findByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
