package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.ProductCreateDto;
import com.pixelpro.catalog.dto.ProductDto;
import com.pixelpro.catalog.dto.ProductUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDto create(ProductCreateDto dto);

    ProductDto findById(Long id);

    Page<ProductDto> findAll(String name, String sku, String status, Long categoryId, Pageable pageable);

    ProductDto update(Long id, ProductUpdateDto dto);

    void delete(Long id);
}
