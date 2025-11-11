package com.pixelpro.attributes.controller;

import com.pixelpro.attributes.dto.AttributeRequest;
import com.pixelpro.attributes.dto.AttributeResponse;
import com.pixelpro.attributes.service.AttributeService;
import com.pixelpro.attributes.service.AttributeValueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/attributes")
@RequiredArgsConstructor
@Tag(name = "Admin - Attributes", description = "Manage product attributes and their values")
public class AdminAttributeController {

    private final AttributeService attributeService;
    private final AttributeValueService valueService;

    @Operation(summary = "Create a new attribute")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Attribute created successfully"),
            @ApiResponse(responseCode = "409", description = "Attribute already exists",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping
    public ResponseEntity<AttributeResponse> create(@Valid @RequestBody AttributeRequest request) {
        AttributeResponse response = attributeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all attributes with their values")
    @ApiResponse(responseCode = "200", description = "List of attributes returned successfully")
    @GetMapping
    public ResponseEntity<List<AttributeResponse>> getAll() {
        List<AttributeResponse> response = attributeService.getAll();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get an attribute by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attribute found"),
            @ApiResponse(responseCode = "404", description = "Attribute not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AttributeResponse> getById(@PathVariable Long id) {
        AttributeResponse response = attributeService.getById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update an attribute")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Attribute updated successfully"),
            @ApiResponse(responseCode = "404", description = "Attribute not found",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Duplicate attribute name",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AttributeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AttributeRequest request
    ) {
        AttributeResponse response = attributeService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an attribute and its values")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Attribute deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Attribute not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attributeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all values for a given attribute")
    @ApiResponse(responseCode = "200", description = "List of attribute values returned successfully")
    @GetMapping("/{id}/values")
    public ResponseEntity<?> getValuesByAttribute(@PathVariable Long id) {
        return ResponseEntity.ok(valueService.getByAttributeId(id));
    }
}
