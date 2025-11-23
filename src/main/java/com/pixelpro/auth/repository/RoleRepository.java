package com.pixelpro.auth.repository;

import com.pixelpro.auth.entity.RoleEntity;
import com.pixelpro.auth.entity.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByRoleName(RoleEnum roleName);
}