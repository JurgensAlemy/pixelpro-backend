package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.entity.CategoryEntity;
import com.pixelpro.catalog.mapper.CategoryMapper;
import com.pixelpro.catalog.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public CategoryDto create(CategoryDto dto) {
        CategoryEntity entity = categoryMapper.toEntity(dto);
        CategoryEntity saved = categoryRepository.save(entity);
        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDto findById(Long id) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toDto)
                .toList();
    }

    @Override
    public CategoryDto update(Long id, CategoryDto dto) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));

        entity.setName(dto.name());

        if (dto.parentCategoryId() != null) {
            CategoryEntity parent = categoryRepository.findById(dto.parentCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id: " + dto.parentCategoryId()));
            entity.setParentCategory(parent);
        } else {
            entity.setParentCategory(null);
        }

        CategoryEntity updated = categoryRepository.save(entity);
        return categoryMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}

