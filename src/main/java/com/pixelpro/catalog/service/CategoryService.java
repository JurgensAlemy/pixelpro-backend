package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.admin.CategoryAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.CategoryAdminResponse;
import com.pixelpro.catalog.dto.admin.CategoryAdminUpdateRequest;
import com.pixelpro.catalog.dto.web.CategoryWebResponse;

import java.util.List;

public interface CategoryService {

    // Admin
    CategoryAdminResponse create(CategoryAdminCreateRequest request);

    CategoryAdminResponse update(Long id, CategoryAdminUpdateRequest request);

    void delete(Long id);

    CategoryAdminResponse findById(Long id);

    List<CategoryAdminResponse> findAllAsTree();

    // Public Web
    List<CategoryWebResponse> findAllRootCategories();

    List<CategoryWebResponse> findSubcategoriesByParent(Long parentId);
}
