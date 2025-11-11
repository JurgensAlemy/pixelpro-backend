package com.pixelpro.catalog.repository;

import com.pixelpro.catalog.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    List<CategoryEntity> findByParentCategoryIsNull();

    List<CategoryEntity> findByParentCategoryId(Long parentCategoryId);

    Optional<CategoryEntity> findByName(String name);

    boolean existsByName(String name);
}
