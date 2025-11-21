package com.pixelpro.catalog.repository;

import com.pixelpro.catalog.entity.ProductEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    boolean existsBySku(String sku);

    @Query("""
            SELECT p FROM ProductEntity p
            WHERE (COALESCE(:name, '') = '' OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
            AND (COALESCE(:sku, '') = '' OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :sku, '%')))
            AND (COALESCE(:status, '') = '' OR p.status = :status)
            AND (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<ProductEntity> findByFilters(
            @Param("name") String name,
            @Param("sku") String sku,
            @Param("status") String status,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );
}
