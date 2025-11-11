package com.pixelpro.catalog.controller.web;

import com.pixelpro.catalog.dto.web.CategoryWebResponse;
import com.pixelpro.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
@Tag(name = "Public - Categories", description = "Endpoints for public category browsing")
public class CategoryWebController {

    private final CategoryService categoryService;

    @Operation(summary = "Get all root categories (top-level)")
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryWebResponse>> findRoots() {
        return ResponseEntity.ok(categoryService.findAllRootCategories());
    }

    @Operation(summary = "Get subcategories of a parent category")
    @GetMapping("/{parentId}/subcategories")
    public ResponseEntity<List<CategoryWebResponse>> findSubcategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.findSubcategoriesByParent(parentId));
    }
}
