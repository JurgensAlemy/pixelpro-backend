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
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para autenticación de usuarios")
public class AuthController {

    private final AuthenticationManager authManager;
    private final UserService userService;
    private final UserRepository userRepository;

    public static final String SPRING_SECURITY_CONTEXT_KEY =
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

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

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        HttpSession session = httpReq.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);

        String rol = getCleanRole(auth);

        return ResponseEntity.ok(new AuthResponse(u.getId(), u.getEmail(), rol, true));
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

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        HttpSession session = httpReq.getSession(true);
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, context);

        UserEntity user = userRepository.findByEmail(req.email()).orElseThrow();

        String rol = getCleanRole(auth);

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), rol, true));
    }

    @GetMapping("/me")
    @Operation(summary = "Información del usuario", description = "Obtiene la información del usuario autenticado")
    public ResponseEntity<AuthResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.ok(new AuthResponse(null, null, null, false));
        }
        String email = auth.getName();
        UserEntity user = userRepository.findByEmail(email).orElse(null);

        String rol = getCleanRole(auth);

        Long id = user != null ? user.getId() : null;
        return ResponseEntity.ok(new AuthResponse(id, email, rol, true));
    }

    private String getCleanRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", "")) // Quitamos el prefijo
                .collect(Collectors.joining(""));
    }
}