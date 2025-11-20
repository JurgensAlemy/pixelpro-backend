package com.pixelpro.auth.service;

import com.pixelpro.auth.entity.RoleEntity;
import com.pixelpro.auth.entity.RoleEnum;
import com.pixelpro.auth.entity.UserEntity;
import com.pixelpro.auth.repository.RoleRepository;
import com.pixelpro.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserEntity register(String email, String rawPassword, RoleEnum roleEnum) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        // si no envían un rol, usar CUSTOMER por defecto
        if (roleEnum == null) {
            roleEnum = RoleEnum.CUSTOMER;
        }
        // Obtener o crear el role
        RoleEnum finalRoleEnum = roleEnum;
        RoleEntity role = roleRepository.findByRoleName(roleEnum)
                .orElseGet(() ->
                        roleRepository.save(RoleEntity.builder().roleName(finalRoleEnum).build())
                );

        UserEntity user = UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .enabled(true)
                .build();
        return userRepository.save(user);
    }
}
