package com.pixelpro.catalog.controller.admin;

import com.pixelpro.catalog.dto.ProductCreateDto;
import com.pixelpro.catalog.dto.ProductDto;
import com.pixelpro.catalog.dto.ProductUpdateDto;
import com.pixelpro.catalog.service.ProductService;
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
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Tag(name = "Admin - Catalog", description = "Endpoints de administración para gestionar el catálogo de productos y categorías")
public class AdminProductController {

    private final ProductService productService;

    @Operation(
            summary = "Crear nuevo producto",
            description = "Permite al administrador crear un nuevo producto en el catálogo. El SKU debe ser único. Se debe asignar una categoría."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Producto creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos (validación fallida)",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría especificada no existe",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe un producto con el SKU especificado",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            )
    })
    @PostMapping
    public ResponseEntity<ProductDto> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del producto a crear",
                    required = true
            )
            @Valid @RequestBody ProductCreateDto dto
    ) {
        ProductDto created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Obtener producto por ID",
            description = "Obtiene los detalles completos de un producto específico mediante su identificador único, incluyendo su categoría asociada."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> findById(
            @Parameter(description = "ID único del producto", example = "1", required = true)
            @PathVariable Long id
    ) {
        ProductDto product = productService.findById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(
            summary = "Listar productos con filtros y paginación",
            description = "Obtiene un listado paginado de productos del catálogo. Permite filtrar por nombre (búsqueda parcial), SKU (búsqueda parcial), estado exacto y categoría. Los resultados se ordenan por fecha de creación descendente por defecto."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<ProductDto>> findAll(
            @Parameter(
                    description = "Filtro por nombre del producto (búsqueda parcial, insensible a mayúsculas)",
                    example = "laptop"
            )
            @RequestParam(required = false) String name,

            @Parameter(
                    description = "Filtro por código SKU (búsqueda parcial, insensible a mayúsculas)",
                    example = "LAP"
            )
            @RequestParam(required = false) String sku,

            @Parameter(
                    description = "Filtro por estado del producto (coincidencia exacta)",
                    example = "ACTIVO",
                    schema = @Schema(allowableValues = {"ACTIVO", "INACTIVO"})
            )
            @RequestParam(required = false) String status,

            @Parameter(
                    description = "Filtro por ID de categoría (obtiene productos de esa categoría)",
                    example = "1"
            )
            @RequestParam(required = false) Long categoryId,

            @Parameter(
                    description = "Parámetros de paginación y ordenamiento. Por defecto: página 0, tamaño 20, ordenado por createdAt DESC",
                    example = "page=0&size=10&sort=name,asc"
            )
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<ProductDto> products = productService.findAll(name, sku, status, categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza los datos de un producto existente. Todos los campos son opcionales. El SKU no se puede modificar. Si se especifica una categoría, reemplaza la categoría actual."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ProductDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado o la categoría especificada no existe",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> update(
            @Parameter(description = "ID único del producto a actualizar", example = "1", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevos datos del producto (campos opcionales)",
                    required = true
            )
            @Valid @RequestBody ProductUpdateDto dto
    ) {
        ProductDto updated = productService.update(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto del catálogo de forma permanente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Producto eliminado exitosamente"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Producto no encontrado",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "ID único del producto a eliminar", example = "1", required = true)
            @PathVariable Long id
    ) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
