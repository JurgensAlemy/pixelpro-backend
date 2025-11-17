package com.pixelpro.catalog.repository;

import com.pixelpro.catalog.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
}
