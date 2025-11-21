package com.pixelpro.catalog.repository;

import com.pixelpro.catalog.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentCategoryId(Long parentCategoryId);

    List<CategoryEntity> findByParentCategoryIsNull();

    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.category.id = :categoryId")
    long countProductsByCategory(@Param("categoryId") Long categoryId);
}
