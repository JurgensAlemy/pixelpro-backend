package com.pixelpro.catalog.controller.store;

import com.pixelpro.catalog.dto.ProductDto;
import com.pixelpro.catalog.service.ProductService;
import com.pixelpro.common.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Store - Catalog", description = "API pública para el catálogo de productos de la tienda")
@RestController
@RequestMapping("/api/public/products")
@RequiredArgsConstructor
public class PublicProductController {

    private final ProductService productService;

    @Operation(
            summary = "Listar productos activos",
            description = "Obtiene un listado paginado de productos activos disponibles en la tienda. " +
                    "Permite filtrar por término de búsqueda (nombre o SKU) y categoría."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de productos obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = Page.class))
    )
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getAllProducts(
            @Parameter(description = "Término de búsqueda para filtrar por nombre o SKU")
            @RequestParam(required = false) String search,
            @Parameter(description = "ID de categoría para filtrar productos")
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        // Forzar filtro de status ACTIVE para productos públicos
        Page<ProductDto> products = productService.findAll(search, "ACTIVO", categoryId, pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Obtener detalle de producto",
            description = "Obtiene la información completa de un producto específico. " +
                    "Solo muestra productos con estado ACTIVE. Si el producto está inactivo, retorna 404."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Producto encontrado exitosamente",
            content = @Content(schema = @Schema(implementation = ProductDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Producto no encontrado o no disponible",
            content = @Content
    )
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(
            @Parameter(description = "ID del producto")
            @PathVariable Long id
    ) {
        ProductDto product = productService.findById(id);

        // Validar que el producto esté activo
        if (!"ACTIVO".equals(product.status())) {
            throw new ResourceNotFoundException("Producto no disponible");
        }

        return ResponseEntity.ok(product);
    }
}


