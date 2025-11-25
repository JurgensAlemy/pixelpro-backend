package com.pixelpro.customers.controller.store;

import com.pixelpro.auth.dto.PasswordUpdateDto;
import com.pixelpro.customers.dto.AddressCreateDto;
import com.pixelpro.customers.dto.AddressDto;
import com.pixelpro.customers.dto.CustomerDto;
import com.pixelpro.customers.dto.CustomerProfileUpdateDto;
import com.pixelpro.customers.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Tag(name = "Store - Mi Cuenta", description = "API para que los clientes gestionen su cuenta y direcciones")
@RestController
@RequestMapping("/api/store/account")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class StoreAccountController {

    private final CustomerService customerService;

    @Operation(
            summary = "Crear mi perfil",
            description = "Crea un perfil de cliente para un usuario recién registrado que aún no tiene CustomerEntity asociado."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Perfil creado exitosamente",
            content = @Content(schema = @Schema(implementation = CustomerDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "Usuario no encontrado",
            content = @Content
    )
    @ApiResponse(
            responseCode = "409",
            description = "El perfil ya existe",
            content = @Content
    )
    @PostMapping("/profile")
    public ResponseEntity<CustomerDto> createProfile(
            @Valid @RequestBody CustomerProfileUpdateDto dto,
            Principal principal
    ) {
        CustomerDto profile = customerService.createProfile(principal.getName(), dto);
        return ResponseEntity.status(201).body(profile);
    }

    @Operation(
            summary = "Obtener mi perfil",
            description = "Obtiene la información del perfil del cliente autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Perfil obtenido exitosamente",
            content = @Content(schema = @Schema(implementation = CustomerDto.class))
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cliente no encontrado",
            content = @Content
    )
    @GetMapping("/profile")
    public ResponseEntity<CustomerDto> getMyProfile(Principal principal) {
        CustomerDto profile = customerService.getMyProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }

    @Operation(
            summary = "Actualizar mi perfil",
            description = "Actualiza la información del perfil del cliente autenticado (nombres, apellidos, teléfono, documento)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Perfil actualizado exitosamente",
            content = @Content(schema = @Schema(implementation = CustomerDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cliente no encontrado",
            content = @Content
    )
    @PutMapping("/profile")
    public ResponseEntity<CustomerDto> updateMyProfile(
            @Valid @RequestBody CustomerProfileUpdateDto dto,
            Principal principal
    ) {
        CustomerDto updated = customerService.updateMyProfile(principal.getName(), dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Cambiar contraseña",
            description = "Permite al cliente cambiar su contraseña de acceso."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Contraseña actualizada exitosamente"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Contraseña inválida",
            content = @Content
    )
    @ApiResponse(
            responseCode = "404",
            description = "Cliente no encontrado",
            content = @Content
    )
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordUpdateDto dto,
            Principal principal
    ) {
        customerService.changePassword(principal.getName(), dto.password());
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Obtener mis direcciones",
            description = "Lista todas las direcciones de envío registradas por el cliente."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Lista de direcciones obtenida exitosamente",
            content = @Content(schema = @Schema(implementation = AddressDto.class))
    )
    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDto>> getMyAddresses(Principal principal) {
        List<AddressDto> addresses = customerService.getMyAddresses(principal.getName());
        return ResponseEntity.ok(addresses);
    }

    @Operation(
            summary = "Agregar nueva dirección",
            description = "Registra una nueva dirección de envío para el cliente autenticado."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dirección creada exitosamente",
            content = @Content(schema = @Schema(implementation = AddressDto.class))
    )
    @ApiResponse(
            responseCode = "400",
            description = "Datos de dirección inválidos",
            content = @Content
    )
    @PostMapping("/addresses")
    public ResponseEntity<AddressDto> addAddress(
            @Valid @RequestBody AddressCreateDto dto,
            Principal principal
    ) {
        AddressDto address = customerService.addAddress(principal.getName(), dto);
        return ResponseEntity.ok(address);
    }

    @Operation(
            summary = "Eliminar dirección",
            description = "Elimina una dirección de envío del cliente. Solo se pueden eliminar direcciones propias."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Dirección eliminada exitosamente"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Dirección no encontrada o no pertenece al cliente",
            content = @Content
    )
    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Void> deleteAddress(
            @Parameter(description = "ID de la dirección a eliminar")
            @PathVariable Long id,
            Principal principal
    ) {
        customerService.deleteAddress(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}

