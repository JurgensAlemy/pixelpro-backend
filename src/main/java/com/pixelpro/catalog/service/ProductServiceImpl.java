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
        CategoryEntity category = loadCategory(dto.categoryId());
        product.setCategory(category);
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
    public Page<ProductDto> findAll(String search, String status, Long categoryId, Pageable pageable) {
        Page<ProductEntity> products = productRepository.findByFilters(search, status, categoryId, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    @Transactional
    public ProductDto update(Long id, ProductUpdateDto dto) {
        ProductEntity product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productMapper.updateEntityFromDto(dto, product);
        if (dto.categoryId() != null) {
            CategoryEntity category = loadCategory(dto.categoryId());
            product.setCategory(category);
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

    private CategoryEntity loadCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + categoryId));
    }
}
