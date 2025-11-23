package com.pixelpro.auth.service;

import com.pixelpro.auth.dto.UserCreateDto;
import com.pixelpro.auth.dto.UserDto;
import com.pixelpro.auth.dto.UserUpdateDto;
import com.pixelpro.auth.entity.RoleEntity;
import com.pixelpro.auth.entity.RoleEnum;
import com.pixelpro.auth.entity.UserEntity;
import com.pixelpro.auth.mapper.UserMapper;
import com.pixelpro.auth.repository.RoleRepository;
import com.pixelpro.auth.repository.UserRepository;
import com.pixelpro.common.exception.ConflictException;
import com.pixelpro.common.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserEntity register(String email, String rawPassword, RoleEnum roleEnum) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        // si no envían un rol, usar CLIENTE por defecto
        if (roleEnum == null) {
            roleEnum = RoleEnum.CLIENTE;
        }
        // Obtener o crear el role
        RoleEnum finalRoleEnum = roleEnum;
        RoleEntity role = roleRepository.findByRoleName(roleEnum)
                .orElseGet(() ->
                        // Si el rol CLIENTE/ADMIN no existe en la BD, lo crea y lo guarda.
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

    // ============ ADMIN METHODS ============

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(RoleEnum role, String search, Pageable pageable) {
        Page<UserEntity> users = userRepository.findAllWithFilters(role, search, pageable);

        return users.map(userMapper::toDto);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto createUser(UserCreateDto dto) {
        UserEntity user = this.register(dto.email(), dto.password(), dto.role());
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        // Validar si el email ya está en uso por otro usuario
        if (!user.getEmail().equals(dto.email()) && userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("El email ya está en uso por otro usuario");
        }

        // Obtener el nuevo rol
        RoleEntity role = roleRepository.findByRoleName(dto.role())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + dto.role()));

        // Actualizar campos
        user.setEmail(dto.email());
        user.setRole(role);
        user.setEnabled(dto.enabled());

        UserEntity updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    @Transactional
    public void updatePassword(Long id, String newPassword) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserDto toggleUserStatus(Long id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));

        user.setEnabled(!user.isEnabled());
        UserEntity updated = userRepository.save(user);
        return userMapper.toDto(updated);
    }

    public List<RoleEnum> getAllRoles() {
        return Arrays.asList(RoleEnum.values());
    }
}
