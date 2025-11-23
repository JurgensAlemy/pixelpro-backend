package com.pixelpro.auth.repository;

import com.pixelpro.auth.entity.RoleEnum;
import com.pixelpro.auth.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u WHERE " +
            "(:role IS NULL OR u.role.roleName = :role) AND " +
            "(:search IS NULL OR :search = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:staffOnly IS NULL OR :staffOnly = false OR u.role.roleName != 'CLIENTE')")
    Page<UserEntity> findAllWithFilters(
            @Param("role") RoleEnum role,
            @Param("search") String search,
            @Param("staffOnly") Boolean staffOnly,
            Pageable pageable
    );
}
