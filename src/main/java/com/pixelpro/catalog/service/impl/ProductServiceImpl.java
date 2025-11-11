package com.pixelpro.catalog.service.impl;

import com.pixelpro.catalog.dto.admin.ProductAdminCreateRequest;
import com.pixelpro.catalog.dto.admin.ProductAdminResponse;
import com.pixelpro.catalog.dto.admin.ProductAdminUpdateRequest;
import com.pixelpro.catalog.dto.admin.ProductImageAdminRequest;
import com.pixelpro.catalog.dto.web.ProductWebResponse;
import com.pixelpro.catalog.entity.CategoryEntity;
import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.catalog.entity.ProductImageEntity;
import com.pixelpro.catalog.mapper.ProductMapper;
import com.pixelpro.catalog.repository.CategoryRepository;
import com.pixelpro.catalog.repository.ProductImageRepository;
import com.pixelpro.catalog.repository.ProductRepository;
import com.pixelpro.catalog.service.ProductService;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductMapper productMapper;

    // Admin
    @Override
    public ProductAdminResponse create(ProductAdminCreateRequest request) {
        // Validate product name uniqueness
        if (productRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("A product with that name already exists.");
        }

        // Map base product entity
        ProductEntity entity = productMapper.toEntity(request);

        // Associate categories (optional)
        if (request.categoryIds() != null && !request.categoryIds().isEmpty()) {
            List<CategoryEntity> categories = categoryRepository.findAllById(request.categoryIds());
            if (categories.size() != request.categoryIds().size()) {
                throw new ResourceNotFoundException("One or more categories not found.");
            }
            entity.setCategories(categories);
        }

        // Associate product images (optional)
        if (request.images() != null) {
            List<ProductImageEntity> images = request.images().stream()
                    .map(imgReq -> {
                        ProductImageEntity img = new ProductImageEntity();
                        img.setUrl(imgReq.url());
                        img.setPosition(imgReq.position());
                        img.setProduct(entity);
                        return img;
                    })
                    .collect(Collectors.toList());
            entity.setImages(images);
        }

        ProductEntity saved = productRepository.save(entity);
        return productMapper.toAdminResponse(saved);
    }

    @Override
    public ProductAdminResponse update(Long id, ProductAdminUpdateRequest request) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + id));

        // Validate name uniqueness if changed
        if (request.name() != null &&
                !request.name().equalsIgnoreCase(product.getName()) &&
                productRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("A product with that name already exists.");
        }

        // Merge non-null fields (PATCH semantics)
        productMapper.updateEntityFromRequest(request, product);

        // Handle categories update (if provided)
        if (request.categoryIds() != null) {
            if (request.categoryIds().isEmpty()) {
                product.getCategories().clear();
            } else {
                List<CategoryEntity> categories = categoryRepository.findAllById(request.categoryIds());
                if (categories.size() != request.categoryIds().size()) {
                    throw new ResourceNotFoundException("One or more categories not found.");
                }
                product.setCategories(categories);
            }
        }

        // Handle images update (if provided)
        if (request.images() != null) {
            product.getImages().clear();
            for (ProductImageAdminRequest imgReq : request.images()) {
                ProductImageEntity img = new ProductImageEntity();
                img.setUrl(imgReq.url());
                img.setPosition(imgReq.position());
                img.setProduct(product);
                product.getImages().add(img);
            }
        }

        ProductEntity updated = productRepository.save(product);
        return productMapper.toAdminResponse(updated);
    }

    @Override
    public void delete(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + id));

        productRepository.delete(product);
    }

    @Override
    public ProductAdminResponse findById(Long id) {
        ProductEntity entity = productRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + id));
        return productMapper.toAdminResponse(entity);
    }

    @Override
    public Page<ProductAdminResponse> findAll(Pageable pageable, String keyword) {
        // ⚙️ Evita errores cuando Swagger envía "string" como valor por defecto
        boolean hasValidKeyword = keyword != null && !keyword.isBlank() && !keyword.equalsIgnoreCase("string");

        Page<ProductEntity> page;

        if (hasValidKeyword) {
            page = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            // Si no hay keyword, devolvemos todo con paginación real
            page = productRepository.findAll(pageable);
        }

        return page.map(productMapper::toAdminResponse);
//        Page<ProductEntity> page;
//        if (keyword != null && !keyword.isBlank()) {
//            page = new PageImpl<>(productRepository.findByNameContainingIgnoreCase(keyword));
//        } else {
//            page = productRepository.findAll(pageable);
//        }
//        return page.map(productMapper::toAdminResponse);
    }

    // Public Web
    @Override
    public Page<ProductWebResponse> findAllPublic(Pageable pageable, String keyword, Long categoryId) {
        List<ProductEntity> products;

        if (categoryId != null) {
            // Filter by category (and keyword if given)
            CategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID " + categoryId));

            products = category.getProducts().stream()
                    .filter(p -> keyword == null || p.getName().toLowerCase().contains(keyword.toLowerCase()))
                    .collect(Collectors.toList());
        } else if (keyword != null && !keyword.isBlank()) {
            products = productRepository.findByNameContainingIgnoreCase(keyword);
        } else {
            products = productRepository.findAll();
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), products.size());
        List<ProductEntity> pageContent = products.subList(start, end);
        return new PageImpl<>(productMapper.toWebResponseList(pageContent), pageable, products.size());
    }

    @Override
    public ProductWebResponse findPublicById(Long id) {
        ProductEntity entity = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID " + id));
        return productMapper.toWebResponse(entity);
    }
}
