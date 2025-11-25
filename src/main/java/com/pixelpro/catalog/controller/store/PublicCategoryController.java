package com.pixelpro.catalog.controller.store;

import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Store - Catalog", description = "API pública para el catálogo de la tienda")
@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
public class PublicCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Listar categorías",
            description = "Obtiene todas las categorías disponibles en la tienda. Opcionalmente puede filtrar por categoría padre."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de categorías obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping
    public ResponseEntity<Page<CategoryDto>> getAllCategories(
            @Parameter(description = "ID de la categoría padre para filtrar subcategorías")
            @RequestParam(required = false) Long parentId,
            @Parameter(description = "Número de página (inicia en 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página")
            @RequestParam(defaultValue = "100") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<CategoryDto> categories = categoryService.findAll(parentId, pageable);
        return ResponseEntity.ok(categories);
    }
}

