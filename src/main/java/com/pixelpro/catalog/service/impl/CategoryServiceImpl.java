package com.pixelpro.catalog.service.impl;

import com.pixelpro.catalog.dto.admin.CategoryAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.CategoryAdminResponse;
import com.pixelpro.catalog.dto.admin.CategoryAdminUpdateRequest;
import com.pixelpro.catalog.dto.web.CategoryWebResponse;
import com.pixelpro.catalog.entity.CategoryEntity;
import com.pixelpro.catalog.mapper.CategoryMapper;
import com.pixelpro.catalog.repository.CategoryRepository;
import com.pixelpro.catalog.service.CategoryService;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    // Admin
    @Override
    public CategoryAdminResponse create(CategoryAdminCreateRequest request) {
        // Validate name uniqueness
        if (categoryRepository.existsByName(request.name())) {
            throw new ConflictException("A category with that name already exists.");
        }

        CategoryEntity entity = categoryMapper.toEntity(request);

        // Validate and set parent category
        if (request.parentId() != null) {
            CategoryEntity parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID " + request.parentId()));
            entity.setParentCategory(parent);
        }

        CategoryEntity saved = categoryRepository.save(entity);
        return categoryMapper.toAdminResponse(saved);
    }

    @Override
    public CategoryAdminResponse update(Long id, CategoryAdminUpdateRequest request) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + id));

        // Check if new name collides with another category
        if (request.name() != null &&
                !request.name().equalsIgnoreCase(category.getName()) &&
                categoryRepository.existsByName(request.name())) {
            throw new ConflictException("A category with that name already exists.");
        }

        // Merge changes
        categoryMapper.updateEntityFromRequest(request, category);

        // Validate new parent (if changed)
        if (request.parentId() != null) {
            if (request.parentId().equals(id)) {
                throw new ConflictException("A category cannot be its own parent.");
            }

            CategoryEntity newParent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID " + request.parentId()));

            // Prevent cyclic hierarchy
            if (isDescendantOf(newParent, category)) {
                throw new ConflictException("Invalid parent: would create a circular reference.");
            }

            category.setParentCategory(newParent);
        }

        CategoryEntity updated = categoryRepository.save(category);
        return categoryMapper.toAdminResponse(updated);
    }

    @Override
    public void delete(Long id) {
        CategoryEntity category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + id));

        // Check if has subcategories
        if (!category.getSubCategories().isEmpty()) {
            throw new ConflictException("Cannot delete category with subcategories.");
        }

        // Check if has associated products
        if (!category.getProducts().isEmpty()) {
            throw new ConflictException("Cannot delete category with associated products.");
        }

        categoryRepository.delete(category);
    }

    @Override
    public CategoryAdminResponse findById(Long id) {
        CategoryEntity entity = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + id));
        return categoryMapper.toAdminResponse(entity);
    }

    @Override
    public List<CategoryAdminResponse> findAllAsTree() {
        List<CategoryEntity> rootCategories = categoryRepository.findByParentCategoryIsNull();
        return categoryMapper.toAdminResponseList(rootCategories);
    }

    // Public Web
    @Override
    public List<CategoryWebResponse> findAllRootCategories() {
        List<CategoryEntity> roots = categoryRepository.findByParentCategoryIsNull();
        return categoryMapper.toWebResponseList(roots);
    }

    @Override
    public List<CategoryWebResponse> findSubcategoriesByParent(Long parentId) {
        List<CategoryEntity> children = categoryRepository.findByParentCategoryId(parentId);
        return categoryMapper.toWebResponseList(children);
    }

    // ---------- UTIL ----------

    /**
     * Utility method to prevent circular hierarchies.
     * Checks if target is a descendant of potentialParent.
     */
    private boolean isDescendantOf(CategoryEntity potentialParent, CategoryEntity target) {
        CategoryEntity current = potentialParent;
        while (current != null) {
            if (current.getId().equals(target.getId())) {
                return true;
            }
            current = current.getParentCategory();
        }
        return false;
    }
}
