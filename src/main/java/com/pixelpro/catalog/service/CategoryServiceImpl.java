package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.CategoryCreateDto;
import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.dto.CategoryUpdateDto;
import com.pixelpro.catalog.entity.CategoryEntity;
import com.pixelpro.catalog.mapper.CategoryMapper;
import com.pixelpro.catalog.repository.CategoryRepository;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public Page<CategoryDto> findAll(Long parentId, Pageable pageable) {
        Page<CategoryEntity> categories;
        // Lógica simplificada: Si hay filtro, busca hijos. Si no, trae todo el catálogo paginado.
        if (parentId != null) {
            categories = categoryRepository.findByParentCategoryId(parentId, pageable);
        } else {
            categories = categoryRepository.findAll(pageable);
        }
        return categories.map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto findById(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto create(CategoryCreateDto dto) {
        CategoryEntity category = categoryMapper.toEntity(dto);
        if (dto.parentCategoryId() != null) {
            CategoryEntity parentCategory = categoryRepository.findById(dto.parentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + dto.parentCategoryId()));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }
        CategoryEntity saved = categoryRepository.save(category);
        return categoryMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CategoryDto update(Long id, CategoryUpdateDto dto) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (dto.parentCategoryId() != null) {
            // Validación de ciclo: Una categoría no puede ser padre de sí misma
            if (dto.parentCategoryId().equals(id)) {
                throw new ConflictException("A category cannot be its own parent");
            }
            CategoryEntity parentCategory = categoryRepository.findById(dto.parentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Parent category not found with id: " + dto.parentCategoryId()));

            // Validación de jerarquía: No puedes ser hijo de tu propio descendiente
            if (isDescendant(parentCategory, id)) {
                throw new ConflictException("Cannot set parent to a descendant category");
            }
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }

        categoryMapper.updateEntityFromDto(dto, category);
        CategoryEntity updated = categoryRepository.save(category);
        return categoryMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        // Validación de integridad: No borrar si tiene productos
        long productCount = categoryRepository.countProductsByCategory(id);
        if (productCount > 0) {
            throw new ConflictException(
                    "Cannot delete category because it is used by " + productCount + " product(s)");
        }

        categoryRepository.deleteById(id);
    }

    private boolean isDescendant(CategoryEntity potentialDescendant, Long ancestorId) {
        CategoryEntity current = potentialDescendant;
        while (current.getParentCategory() != null) {
            if (current.getParentCategory().getId().equals(ancestorId)) {
                return true;
            }
            current = current.getParentCategory();
        }
        return false;
    }
}