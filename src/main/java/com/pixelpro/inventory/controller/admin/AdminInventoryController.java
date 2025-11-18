package com.pixelpro.inventory.controller.admin;

import com.pixelpro.inventory.dto.InventoryDto;
import com.pixelpro.inventory.service.InventoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory")
public class AdminInventoryController {

    private final InventoryService inventoryService;

    public AdminInventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryDto> create(@RequestBody InventoryDto inventoryDto) {
        InventoryDto created = inventoryService.create(inventoryDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDto> findById(@PathVariable Long id) {
        InventoryDto inventory = inventoryService.findById(id);
        return ResponseEntity.ok(inventory);
    }

    @GetMapping
    public ResponseEntity<List<InventoryDto>> findAll() {
        List<InventoryDto> inventories = inventoryService.findAll();
        return ResponseEntity.ok(inventories);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> update(@PathVariable Long id, @RequestBody InventoryDto inventoryDto) {
        InventoryDto updated = inventoryService.update(id, inventoryDto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

