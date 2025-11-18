package com.pixelpro.catalog.service;

import com.pixelpro.catalog.dto.ProductDto;

import java.util.List;

public interface ProductService {

    ProductDto create(ProductDto dto);

    ProductDto findById(Long id);

    List<ProductDto> findAll();

    ProductDto update(Long id, ProductDto dto);

    void delete(Long id);
}

