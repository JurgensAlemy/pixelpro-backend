package com.pixelpro.catalog.repository;

import com.pixelpro.catalog.entity.ProductImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImageEntity, Long> {
    List<ProductImageEntity> findByProductIdOrderByPositionAsc(Long productId);
}
