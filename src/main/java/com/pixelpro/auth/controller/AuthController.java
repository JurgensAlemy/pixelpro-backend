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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody; // ✅ Import correcto de Spring
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

    @Operation(summary = "Registrar nuevo usuario", description = "Crea un nuevo usuario y lo autentica")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest req, // ✅ Ahora sí usa el @RequestBody de Spring
            HttpServletRequest httpReq
    ) {
        UserEntity u = userService.register(req.email(), req.password(), null);
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = httpReq.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new AuthResponse(u.getId(), u.getEmail(), roles, true));
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica un usuario existente")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req, // ✅ Limpieza del código (ya no usamos el nombre completo)
            HttpServletRequest httpReq
    ) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
        HttpSession session = httpReq.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        UserEntity user = userRepository.findByEmail(req.email()).orElseThrow();
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getEmail(), roles, true));
    }

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
