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
    public UserEntity register(String email, String rawPassword, Set<RoleEnum> roles) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        UserEntity user = UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();
//        user.setEnabled(true);
        
        // Roles por defecto
        if (roles == null || roles.isEmpty()) {
            RoleEntity userRole = roleRepository.findByRoleName(RoleEnum.CUSTOMER)
                    .orElseGet(() -> roleRepository.save(RoleEntity.builder().roleName(RoleEnum.CUSTOMER).build()));
            user.getRoles().add(userRole);
        } else {
            for (RoleEnum roleEnum : roles) {
                RoleEntity r = roleRepository.findByRoleName(roleEnum)
                        .orElseGet(() -> roleRepository.save(RoleEntity.builder().roleName(roleEnum).build()));
                user.getRoles().add(r);
            }
        }
        return userRepository.save(user);
    }
}
