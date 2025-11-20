package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.CategoryCreateDto;
import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.dto.CategoryUpdateDto;

import java.util.List;

public interface CategoryService {
    CategoryDto create(CategoryCreateDto dto);

    CategoryDto findById(Long id);

    List<CategoryDto> findAll();

    List<CategoryDto> findByParentId(Long parentId);

    CategoryDto update(Long id, CategoryUpdateDto dto);

    void delete(Long id);
}
