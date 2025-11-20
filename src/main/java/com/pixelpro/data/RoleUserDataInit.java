package com.pixelpro.data;

import com.pixelpro.auth.entity.RoleEntity;
import com.pixelpro.auth.entity.RoleEnum;
import com.pixelpro.auth.repository.RoleRepository;
import com.pixelpro.auth.repository.UserRepository;
import com.pixelpro.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
@RequiredArgsConstructor
public class RoleUserDataInit implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(String... args) {
        System.out.println("🚀 Inicializando roles y usuario admin...");

        // 1️⃣ Crear roles si no existen
        for (RoleEnum roleEnum : RoleEnum.values()) {
            roleRepository.findByRoleName(roleEnum)
                    .orElseGet(() -> {
                        RoleEntity role = RoleEntity.builder()
                                .roleName(roleEnum)
                                .build();
                        return roleRepository.save(role);
                    });
        }

        // 2️⃣ Crear usuario admin por defecto
        if (userRepository.findByEmail("admin@pixelpro.com").isEmpty()) {

            // El método en UserService.java se llama 'register' y espera un Set de roles.
            userService.register("admin@pixelpro.com", "admin123", RoleEnum.ADMIN);

            System.out.println("✅ Usuario admin creado: admin@pixelpro.com / admin123");
        } else {
            System.out.println("⚠️ Usuario admin ya existe, se omite creación.");
        }
    }
}