package com.pixelpro.auth.controller;

import com.pixelpro.auth.dto.AuthResponse;
import com.pixelpro.auth.dto.LoginRequest;
import com.pixelpro.auth.dto.RegisterRequest;
import com.pixelpro.auth.entity.UserEntity;
import com.pixelpro.auth.repository.UserRepository;
import com.pixelpro.auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final UserRepository userRepository;

    // Esta es la llave que Spring usa para buscar la sesión
    public static final String SPRING_SECURITY_CONTEXT_KEY = HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario y lo autentica")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req,
            HttpServletRequest httpReq
    ) {
        UserEntity u = userService.register(req.email(), req.password(), null);
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // --- 1. CORRECCIÓN EN REGISTER ---
        // ¡Creamos un contexto NUEVO, no usamos el viejo!
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        HttpSession session = httpReq.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context); // Usamos la llave correcta
        // --- FIN DE LA CORRECCIÓN ---

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new AuthResponse(u.getId(), u.getEmail(), roles, true));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario existente")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest httpReq
    ) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        // --- 2. CORRECCIÓN EN LOGIN ---
        // ¡Creamos un contexto NUEVO, no usamos el viejo!
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);

        HttpSession session = httpReq.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context); // Usamos la llave correcta
        // --- FIN DE LA CORRECCIÓN ---

        UserEntity user = userRepository.findByEmail(req.email()).orElseThrow();
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), roles, true));
    }

    // Tu método /me está perfecto, no lo toques
    @GetMapping("/me")
    @Operation(summary = "Información del usuario", description = "Obtiene la información del usuario autenticado")
    public ResponseEntity<AuthResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok(new AuthResponse(null, null, Set.of(), false));
        }
        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        Long id = user != null ? user.getId() : null;
        return ResponseEntity.ok(new AuthResponse(id, email, roles, true));
    }
}