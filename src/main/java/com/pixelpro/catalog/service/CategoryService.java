package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.CategoryCreateDto;
import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.dto.CategoryUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    Page<CategoryDto> findAll(Long parentId, Pageable pageable);

    CategoryDto findById(Long id);

    CategoryDto create(CategoryCreateDto dto);

    CategoryDto update(Long id, CategoryUpdateDto dto);

    void delete(Long id);
}