package com.pixelpro.inventory.service;

import com.pixelpro.inventory.dto.InventoryDto;

import java.util.List;

public interface InventoryService {

    InventoryDto create(InventoryDto dto);

    InventoryDto findById(Long id);

    List<InventoryDto> findAll();

    InventoryDto update(Long id, InventoryDto dto);

    void delete(Long id);
}

