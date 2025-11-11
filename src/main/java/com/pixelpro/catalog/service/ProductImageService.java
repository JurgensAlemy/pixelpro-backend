package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.admin.ProductImageAdminRequest;
import com.pixelpro.catalog.dto.admin.ProductImageAdminResponse;
import com.pixelpro.catalog.dto.web.ProductImageWebResponse;

import java.util.List;

public interface ProductImageService {

    // Admin
    ProductImageAdminResponse addImageToProduct(Long productId, ProductImageAdminRequest request);

    ProductImageAdminResponse updateImage(Long imageId, ProductImageAdminRequest request);

    void deleteImage(Long imageId);

    List<ProductImageAdminResponse> findByProduct(Long productId);

    // Public Web
    List<ProductImageWebResponse> findPublicByProduct(Long productId);
}
