package com.pixelpro.catalog.service.impl;

import com.pixelpro.catalog.dto.admin.ProductImageAdminRequest;
import com.pixelpro.catalog.dto.admin.ProductImageAdminResponse;
import com.pixelpro.catalog.dto.web.ProductImageWebResponse;
import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.catalog.entity.ProductImageEntity;
import com.pixelpro.catalog.mapper.ProductImageMapper;
import com.pixelpro.catalog.repository.ProductImageRepository;
import com.pixelpro.catalog.repository.ProductRepository;
import com.pixelpro.catalog.service.ProductImageService;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageServiceImpl implements ProductImageService {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductImageMapper productImageMapper;

    // Admin
    @Override
    public ProductImageAdminResponse addImageToProduct(Long productId, ProductImageAdminRequest request) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + productId));

        // Check for duplicate position
        boolean positionExists = product.getImages().stream()
                .anyMatch(img -> img.getPosition().equals(request.position()));
        if (positionExists) {
            throw new ConflictException("Position " + request.position() + " already exists for this product.");
        }

        ProductImageEntity image = productImageMapper.toEntity(request);
        image.setProduct(product);
        ProductImageEntity saved = productImageRepository.save(image);

        return productImageMapper.toAdminResponse(saved);
    }

    @Override
    public ProductImageAdminResponse updateImage(Long imageId, ProductImageAdminRequest request) {
        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID " + imageId));

        // If changing position, check conflict
        if (request.position() != null) {
            boolean positionTaken = productImageRepository
                    .findByProductIdOrderByPositionAsc(image.getProduct().getId())
                    .stream()
                    .anyMatch(img -> !img.getId().equals(imageId) && img.getPosition().equals(request.position()));
            if (positionTaken) {
                throw new ConflictException("Position " + request.position() + " is already used by another image.");
            }
            image.setPosition(request.position());
        }

        if (request.url() != null) {
            image.setUrl(request.url());
        }

        ProductImageEntity updated = productImageRepository.save(image);
        return productImageMapper.toAdminResponse(updated);
    }

    @Override
    public void deleteImage(Long imageId) {
        ProductImageEntity image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found with ID " + imageId));

        productImageRepository.delete(image);
    }

    @Override
    public List<ProductImageAdminResponse> findByProduct(Long productId) {
        List<ProductImageEntity> images = productImageRepository.findByProductIdOrderByPositionAsc(productId);
        return productImageMapper.toAdminResponseList(images);
    }

    // Public Web
    @Override
    public List<ProductImageWebResponse> findPublicByProduct(Long productId) {
        List<ProductImageEntity> images = productImageRepository.findByProductIdOrderByPositionAsc(productId);
        return productImageMapper.toWebResponseList(images);
    }
}
