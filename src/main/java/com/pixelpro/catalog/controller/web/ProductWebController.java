package com.pixelpro.catalog.controller.web;

import com.pixelpro.catalog.dto.web.ProductWebResponse;
import com.pixelpro.catalog.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
@Tag(name = "Public - Products", description = "Endpoints for public product catalog")
public class ProductWebController {

    private final ProductService productService;

    @Operation(summary = "List public products with pagination, optional search, and category filter")
    @GetMapping
    public ResponseEntity<Page<ProductWebResponse>> findAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(productService.findAllPublic(pageable, keyword, categoryId));
    }

    @Operation(summary = "Get public product detail by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ProductWebResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findPublicById(id));
    }
}
