package com.pixelpro.auth.repository;

import com.pixelpro.auth.entity.RoleEntity;
import com.pixelpro.auth.entity.RoleEnum; // Importamos el Enum
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional; // Importamos Optional

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    // --- ¡ESTA ES LA LÍNEA QUE FALTABA! ---
    // Este método es el que busca 'RoleUserDataInit.java'
    // Spring Data JPA lo implementará automáticamente
    Optional<RoleEntity> findByRoleName(RoleEnum roleName);
}