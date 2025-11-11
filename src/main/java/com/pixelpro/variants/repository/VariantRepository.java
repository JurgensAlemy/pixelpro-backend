package com.pixelpro.variants.repository;

import com.pixelpro.variants.entity.VariantEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VariantRepository extends JpaRepository<VariantEntity, Long> {
}
