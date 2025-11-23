package com.pixelpro.auth.controller;

import com.pixelpro.auth.dto.PasswordUpdateDto;
import com.pixelpro.auth.dto.UserCreateDto;
import com.pixelpro.auth.dto.UserDto;
import com.pixelpro.auth.dto.UserUpdateDto;
import com.pixelpro.auth.entity.RoleEnum;
import com.pixelpro.auth.service.UserService;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin - Users", description = "Endpoints de administración para gestionar usuarios del sistema")
public class AdminUserController {

    private final UserService userService;

    @Operation(
            summary = "Listar todos los usuarios",
            description = "Obtiene un listado paginado de todos los usuarios del sistema con su información básica y rol asignado. Los resultados se ordenan por última actualización descendente por defecto. Permite filtrar por rol (ADMIN/CLIENTE), buscar por email y filtrar solo personal administrativo (excluye clientes)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de usuarios obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = Page.class))
            )
    })
    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(
                    description = "Filtrar usuarios por rol específico (ADMIN o CLIENTE)",
                    example = "ADMIN"
            )
            @RequestParam(required = false) RoleEnum role,

            @Parameter(
                    description = "Buscar usuarios por email (búsqueda parcial, insensible a mayúsculas/minúsculas)",
                    example = "admin@pixelpro.com"
            )
            @RequestParam(required = false) String search,

            @Parameter(
                    description = "Si es true, muestra solo personal administrativo (excluye usuarios con rol CLIENTE). Por defecto: false (muestra todos los usuarios)",
                    example = "true"
            )
            @RequestParam(defaultValue = "false") Boolean staffOnly,

            @Parameter(
                    description = "Parámetros de paginación y ordenamiento. Por defecto: página 0, tamaño 20, ordenado por updatedAt DESC (última actualización)",
                    example = "page=0&size=10&sort=email,asc"
            )
            @PageableDefault(size = 20, sort = "updatedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<UserDto> users = userService.getAllUsers(role, search, staffOnly, pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
            summary = "Crear nuevo usuario",
            description = "Permite al administrador crear un nuevo usuario en el sistema. Se debe proporcionar email único, contraseña y rol. El usuario se crea en estado activo por defecto."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos (validación fallida)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El email ya está registrado en el sistema",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PostMapping
    public ResponseEntity<UserDto> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos del usuario a crear",
                    required = true
            )
            @Valid @RequestBody UserCreateDto dto
    ) {
        UserDto created = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(
            summary = "Obtener usuario por ID",
            description = "Obtiene los detalles completos de un usuario específico mediante su identificador único."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "ID único del usuario", example = "1", required = true)
            @PathVariable Long id
    ) {
        UserDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(
            summary = "Actualizar usuario",
            description = "Permite al administrador actualizar la información de un usuario existente. Se puede modificar el email, rol y estado de activación. No incluye actualización de contraseña (usar el endpoint específico)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos de entrada inválidos (validación fallida)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El email ya está en uso por otro usuario",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @Parameter(description = "ID único del usuario a actualizar", example = "1", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nuevos datos del usuario",
                    required = true
            )
            @Valid @RequestBody UserUpdateDto dto
    ) {
        UserDto updated = userService.updateUser(id, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Actualizar contraseña de usuario",
            description = "Permite al administrador cambiar la contraseña de un usuario. La contraseña se almacena de forma segura usando bcrypt."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Contraseña actualizada exitosamente"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Contraseña inválida (debe tener al menos 8 caracteres)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PatchMapping("/{id}/password")
    public ResponseEntity<Void> updatePassword(
            @Parameter(description = "ID único del usuario", example = "1", required = true)
            @PathVariable Long id,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Nueva contraseña",
                    required = true
            )
            @Valid @RequestBody PasswordUpdateDto dto
    ) {
        userService.updatePassword(id, dto.password());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Cambiar estado de activación del usuario",
            description = "Alterna el estado de activación del usuario entre activo (enabled=true) e inactivo (enabled=false). Los usuarios inactivos no pueden iniciar sesión en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del usuario cambiado exitosamente",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario no encontrado",
                    content = @Content(schema = @Schema(implementation = ApiError.class))
            )
    })
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<UserDto> toggleUserStatus(
            @Parameter(description = "ID único del usuario", example = "1", required = true)
            @PathVariable Long id
    ) {
        UserDto updated = userService.toggleUserStatus(id);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Obtener lista de roles disponibles",
            description = "Retorna la lista de todos los roles disponibles en el sistema que pueden ser asignados a los usuarios."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de roles obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    @GetMapping("/roles")
    public ResponseEntity<List<RoleEnum>> getAllRoles() {
        List<RoleEnum> roles = userService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @Operation(
            summary = "Obtener lista de roles de personal administrativo",
            description = "Retorna la lista de roles disponibles para el personal administrativo del sistema, excluyendo el rol CLIENTE. Este endpoint es útil para formularios de gestión de staff donde no se debe permitir asignar el rol de cliente."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de roles de staff obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    @GetMapping("/roles/staff")
    public ResponseEntity<List<RoleEnum>> getStaffRoles() {
        List<RoleEnum> staffRoles = userService.getStaffRoles();
        return ResponseEntity.ok(staffRoles);
    }
}

