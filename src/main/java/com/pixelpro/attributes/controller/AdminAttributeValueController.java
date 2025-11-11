package com.pixelpro.attributes.controller;

import com.pixelpro.attributes.dto.AttributeValueRequest;
import com.pixelpro.attributes.dto.AttributeValueResponse;
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

@RestController
@RequestMapping("/api/admin/attribute-values")
@RequiredArgsConstructor
@Tag(name = "Admin - Attribute Values", description = "Manage values of attributes")
public class AdminAttributeValueController {

    private final AttributeValueService service;

    @Operation(summary = "Create a new value for a given attribute")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Value created successfully"),
            @ApiResponse(responseCode = "404", description = "Attribute not found",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Value already exists in this attribute",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PostMapping("/attribute/{attributeId}")
    public ResponseEntity<AttributeValueResponse> create(
            @PathVariable Long attributeId,
            @Valid @RequestBody AttributeValueRequest request
    ) {
        var response = service.create(attributeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Update an attribute value")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Value updated successfully"),
            @ApiResponse(responseCode = "404", description = "Value not found",
                    content = @Content(schema = @Schema(hidden = true))),
            @ApiResponse(responseCode = "409", description = "Duplicate value name",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AttributeValueResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AttributeValueRequest request
    ) {
        var response = service.update(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an attribute value")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Value deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Value not found",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
