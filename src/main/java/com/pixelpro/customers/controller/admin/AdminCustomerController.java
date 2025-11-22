package com.pixelpro.customers.controller.admin;

import com.pixelpro.customers.dto.CustomerDto;
import com.pixelpro.customers.dto.CustomerUpdateDto;
import com.pixelpro.customers.entity.enums.CustomerType;
import com.pixelpro.customers.entity.enums.DocumentType;
import com.pixelpro.customers.service.CustomerService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
@Tag(name = "Admin - Customers", description = "Endpoints de administración para gestionar clientes")
public class AdminCustomerController {

    private final CustomerService customerService;

    @Operation(
            summary = "Listar clientes con filtros y paginación",
            description = "Obtiene un listado paginado de clientes. Permite filtrar mediante búsqueda global (nombre, apellido, email o número de documento) y filtros específicos por tipo de documento y tipo de cliente. Los resultados se ordenan por fecha de creación descendente por defecto."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de clientes obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
            @Parameter(
                    description = "Término de búsqueda global que filtra por nombre, apellido, email o número de documento (búsqueda parcial, insensible a mayúsculas)",
                    example = "Juan"
            )
            @RequestParam(required = false) String search,

            @Parameter(
                    description = "Filtro por tipo de documento (coincidencia exacta)",
                    example = "DNI",
                    schema = @Schema(allowableValues = {"DNI", "RUC", "PASAPORTE"})
            )
            @RequestParam(required = false) DocumentType documentType,

            @Parameter(
                    description = "Filtro por tipo de cliente (coincidencia exacta)",
                    example = "NATURAL",
                    schema = @Schema(allowableValues = {"NATURAL", "JURIDICA"})
            )
            @RequestParam(required = false) CustomerType customerType,

            @Parameter(
                    description = "Parámetros de paginación y ordenamiento. Por defecto: página 0, tamaño 20, ordenado por createdAt DESC",
                    example = "page=0&size=10&sort=firstName,asc"
            )
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<CustomerDto> customers = customerService.getAllCustomers(search, documentType, customerType, pageable);
        return ResponseEntity.ok(customers);
    }

    @Operation(
            summary = "Obtener cliente por ID",
            description = "Obtiene los detalles completos de un cliente específico mediante su identificador único, incluyendo todas sus direcciones registradas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "ID único del cliente", example = "1", required = true)
            @PathVariable Long id
    ) {
        CustomerDto customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @Operation(
            summary = "Actualizar información del cliente",
            description = "Permite al administrador actualizar información básica del cliente como email y teléfono. Los campos son opcionales (actualización parcial)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = CustomerDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos (validación fallida)",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado",
                    content = @Content(schema = @Schema(implementation = com.pixelpro.common.exception.ApiError.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "ID único del cliente a actualizar", example = "1", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevos datos del cliente (campos opcionales)",
                    required = true
            )
            @Valid @RequestBody CustomerUpdateDto dto
    ) {
        CustomerDto updated = customerService.updateCustomer(id, dto);
        return ResponseEntity.ok(updated);
    }
}

