package com.pixelpro.catalog.controller.admin;

import com.pixelpro.catalog.dto.admin.ProductAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.ProductAdminResponse;
import com.pixelpro.catalog.dto.admin.ProductAdminUpdateRequest;
import com.pixelpro.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Products", description = "Endpoints for managing products")
public class ProductAdminController {

    private final ProductService productService;

    @Operation(summary = "Create a new product")
    @PostMapping
    public ResponseEntity<ProductAdminResponse> create(@Valid @RequestBody ProductAdminCreateRequest request) {
        return ResponseEntity.ok(productService.create(request));
    }

    @Operation(summary = "Update an existing product (partial update)")
    @PatchMapping("/{id}")
    public ResponseEntity<ProductAdminResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductAdminUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @Operation(summary = "Delete a product by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get product detail by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductAdminResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @Operation(summary = "List all products with pagination and optional search")
    @GetMapping
    public ResponseEntity<Page<ProductAdminResponse>> findAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.findAll(pageable, keyword));
    }
}
