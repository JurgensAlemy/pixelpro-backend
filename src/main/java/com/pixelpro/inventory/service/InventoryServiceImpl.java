package com.pixelpro.inventory.service;

import com.pixelpro.inventory.dto.InventoryDto;
import com.pixelpro.inventory.entity.InventoryEntity;
import com.pixelpro.inventory.mapper.InventoryMapper;
import com.pixelpro.inventory.repository.InventoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, InventoryMapper inventoryMapper) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Override
    public InventoryDto create(InventoryDto dto) {
        InventoryEntity entity = inventoryMapper.toEntity(dto);
        InventoryEntity saved = inventoryRepository.save(entity);
        return inventoryMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryDto findById(Long id) {
        InventoryEntity entity = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + id));
        return inventoryMapper.toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryDto> findAll() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toDto)
                .toList();
    }

    @Override
    public InventoryDto update(Long id, InventoryDto dto) {
        InventoryEntity entity = inventoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Inventory not found with id: " + id));

        entity.setQtyStock(dto.qtyStock());

        InventoryEntity updated = inventoryRepository.save(entity);
        return inventoryMapper.toDto(updated);
    }

    @Override
    public void delete(Long id) {
        if (!inventoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Inventory not found with id: " + id);
        }
        inventoryRepository.deleteById(id);
    }
}

