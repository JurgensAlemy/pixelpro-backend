package com.pixelpro.attributes.repository;

import com.pixelpro.attributes.entity.AttributeValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeValueRepository extends JpaRepository<AttributeValueEntity, Long> {
    List<AttributeValueEntity> findByAttribute_Id(Long attributeId);

    Optional<AttributeValueEntity> findByAttribute_IdAndAttributeValueNameIgnoreCase(Long attributeId, String valueName);

    boolean existsByAttribute_IdAndAttributeValueNameIgnoreCase(Long attributeId, String valueName);
}
