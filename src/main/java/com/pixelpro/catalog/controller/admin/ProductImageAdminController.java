package com.pixelpro.catalog.controller.admin;

import com.pixelpro.catalog.dto.admin.ProductImageAdminRequest;
import com.pixelpro.catalog.dto.admin.ProductImageAdminResponse;
import com.pixelpro.catalog.service.ProductImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin - Product Images", description = "Endpoints for managing product images")
public class ProductImageAdminController {

    private final ProductImageService productImageService;

    @Operation(summary = "Add a new image to a product")
    @PostMapping("/products/{productId}/images")
    public ResponseEntity<ProductImageAdminResponse> addImage(
            @PathVariable Long productId,
            @Valid @RequestBody ProductImageAdminRequest request
    ) {
        return ResponseEntity.ok(productImageService.addImageToProduct(productId, request));
    }

    @Operation(summary = "Update a product image (URL or position)")
    @PatchMapping("/product-images/{imageId}")
    public ResponseEntity<ProductImageAdminResponse> update(
            @PathVariable Long imageId,
            @Valid @RequestBody ProductImageAdminRequest request
    ) {
        return ResponseEntity.ok(productImageService.updateImage(imageId, request));
    }

    @Operation(summary = "Delete a product image")
    @DeleteMapping("/product-images/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable Long imageId) {
        productImageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List all images of a product")
    @GetMapping("/products/{productId}/images")
    public ResponseEntity<List<ProductImageAdminResponse>> findByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productImageService.findByProduct(productId));
    }
}
