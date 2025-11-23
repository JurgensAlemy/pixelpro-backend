package com.pixelpro.catalog.repository;

import com.pixelpro.catalog.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    // Solo mantenemos la versión paginada
    Page<CategoryEntity> findByParentCategoryId(Long parentCategoryId, Pageable pageable);

    // Este query es eficiente para validaciones antes de borrar
    @Query("SELECT COUNT(p) FROM ProductEntity p WHERE p.category.id = :categoryId")
    long countProductsByCategory(@Param("categoryId") Long categoryId);
}