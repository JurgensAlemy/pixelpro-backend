package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.CategoryDto;

import java.util.List;

public interface CategoryService {

    CategoryDto create(CategoryDto dto);

    CategoryDto findById(Long id);

    List<CategoryDto> findAll();

    CategoryDto update(Long id, CategoryDto dto);

    void delete(Long id);
}

