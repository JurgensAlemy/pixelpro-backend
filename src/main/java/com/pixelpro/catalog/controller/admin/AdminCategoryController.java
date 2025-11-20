package com.pixelpro.catalog.controller.admin;

import com.pixelpro.catalog.dto.CategoryCreateDto;
import com.pixelpro.catalog.dto.CategoryDto;
import com.pixelpro.catalog.dto.CategoryUpdateDto;
import com.pixelpro.catalog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Tag(name = "Admin - Catalog", description = "Endpoints de administración para gestionar el catálogo de productos y categorías")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(
        summary = "Crear nueva categoría",
        description = "Permite al administrador crear una nueva categoría de productos. Se puede especificar una categoría padre para crear jerarquías."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Categoría creada exitosamente",
            content = @Content(schema = @Schema(implementation = CategoryDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos (validación fallida)",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría padre no encontrada",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        )
    })
    @PostMapping
    public ResponseEntity<CategoryDto> create(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos de la categoría a crear",
            required = true
        )
        @Valid @RequestBody CategoryCreateDto dto
    ) {
        CategoryDto created = categoryService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
        summary = "Obtener categoría por ID",
        description = "Obtiene los detalles completos de una categoría específica mediante su identificador único."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoría encontrada",
            content = @Content(schema = @Schema(implementation = CategoryDto.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> findById(
        @Parameter(description = "ID único de la categoría", example = "1", required = true)
        @PathVariable Long id
    ) {
        CategoryDto category = categoryService.findById(id);
        return ResponseEntity.ok(category);
    }

    @Operation(
        summary = "Listar categorías",
        description = "Obtiene el listado de todas las categorías del catálogo. Opcionalmente se puede filtrar por categoría padre para obtener subcategorías específicas."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de categorías obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = CategoryDto.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<CategoryDto>> findAll(
        @Parameter(
            description = "ID de la categoría padre para filtrar subcategorías. Si no se especifica, devuelve todas las categorías",
            example = "1"
        )
        @RequestParam(required = false) Long parentId
    ) {
        List<CategoryDto> categories;
        if (parentId != null) {
            categories = categoryService.findByParentId(parentId);
        } else {
            categories = categoryService.findAll();
        }
        return ResponseEntity.ok(categories);
    }

    @Operation(
        summary = "Actualizar categoría",
        description = "Actualiza los datos de una categoría existente. Permite cambiar el nombre y/o la categoría padre. No se permite que una categoría sea su propio padre ni que se creen ciclos en la jerarquía."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Categoría actualizada exitosamente",
            content = @Content(schema = @Schema(implementation = CategoryDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Conflicto: la categoría no puede ser su propio padre o se detectó un ciclo en la jerarquía",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> update(
        @Parameter(description = "ID único de la categoría a actualizar", example = "1", required = true)
        @PathVariable Long id,

        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nuevos datos de la categoría",
            required = true
        )
        @Valid @RequestBody CategoryUpdateDto dto
    ) {
        CategoryDto updated = categoryService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Eliminar categoría",
        description = "Elimina una categoría del catálogo. No se puede eliminar si tiene productos asociados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Categoría eliminada exitosamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Categoría no encontrada",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "No se puede eliminar la categoría porque tiene productos asociados",
            content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @Parameter(description = "ID único de la categoría a eliminar", example = "1", required = true)
        @PathVariable Long id
    ) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
