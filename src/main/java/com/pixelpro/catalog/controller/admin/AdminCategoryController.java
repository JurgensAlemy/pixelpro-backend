package com.pixelpro.catalog.controller.admin;

import com.pixelpro.catalog.dto.CategoryCreateDto;
import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.dto.CategoryUpdateDto;
import com.pixelpro.catalog.service.CategoryService;
import com.pixelpro.common.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin - Catalog", description = "Endpoints de administración para gestionar el catálogo de productos y categorías")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(
            summary = "Listar categorías con paginación",
            description = "Obtiene un listado paginado de todas las categorías del catálogo. Opcionalmente se puede filtrar por categoría padre para obtener subcategorías específicas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de categorías obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<CategoryDto>> findAll(
            @Parameter(
                    description = "ID de la categoría padre para filtrar subcategorías. Si no se especifica, devuelve todas las categorías",
                    example = "1"
            )
            @RequestParam(required = false) Long parentId,

            @Parameter(
                    description = "Parámetros de paginación y ordenamiento. Por defecto: página 0, tamaño 20, ordenado por última actualización descendente",
                    example = "page=0&size=10&sort=updatedAt,desc"
            )
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<CategoryDto> categories = categoryService.findAll(parentId, pageable);
        return ResponseEntity.ok(categories);
    }

    @Operation(summary = "Obtener categoría por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content(schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> findById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @Operation(summary = "Crear nueva categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoría padre no encontrada")
    })
    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody CategoryCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.create(dto));
    }

    @Operation(summary = "Actualizar categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada"),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada"),
            @ApiResponse(responseCode = "409", description = "Conflicto de jerarquía")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(@PathVariable Long id, @Valid @RequestBody CategoryUpdateDto dto) {
        return ResponseEntity.ok(categoryService.update(id, dto));
    }

    @Operation(summary = "Eliminar categoría")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoría eliminada"),
            @ApiResponse(responseCode = "409", description = "No se puede eliminar (tiene productos asociados)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}