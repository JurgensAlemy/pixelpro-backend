package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.ProductCreateDto;
import com.pixelpro.catalog.dto.ProductDto;
import com.pixelpro.catalog.dto.ProductUpdateDto;
import com.pixelpro.catalog.entity.CategoryEntity;
import com.pixelpro.catalog.entity.ProductEntity;
import com.pixelpro.catalog.mapper.ProductMapper;
import com.pixelpro.catalog.repository.CategoryRepository;
import com.pixelpro.catalog.repository.ProductRepository;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDto create(ProductCreateDto dto) {
        if (productRepository.existsBySku(dto.sku())) {
            throw new ConflictException("Product with SKU '" + dto.sku() + "' already exists");
        }
        ProductEntity product = productMapper.toEntity(dto);
        List<CategoryEntity> categories = loadCategories(dto.categoryIds());
        product.setCategories(categories);
        ProductEntity saved = productRepository.save(product);
        return productMapper.toDto(saved);
    }

    @Override
    public ProductDto findById(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Override
    public Page<ProductDto> findAll(String name, String sku, String status, Long categoryId, Pageable pageable) {
        Page<ProductEntity> products = productRepository.findByFilters(name, sku, status, categoryId, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    @Transactional
    public ProductDto update(Long id, ProductUpdateDto dto) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productMapper.updateEntityFromDto(dto, product);
        if (dto.categoryIds() != null && !dto.categoryIds().isEmpty()) {
            List<CategoryEntity> categories = loadCategories(dto.categoryIds());
            product.getCategories().clear();
            product.getCategories().addAll(categories);
        }
        ProductEntity updated = productRepository.save(product);
        return productMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    private List<CategoryEntity> loadCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new ConflictException("At least one category is required");
        }
        List<CategoryEntity> categories = new ArrayList<>();
        for (Long categoryId : categoryIds) {
            CategoryEntity category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found with id: " + categoryId));
            categories.add(category);
        }
        return categories;
    }
}
