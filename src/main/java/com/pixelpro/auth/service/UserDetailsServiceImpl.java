package com.pixelpro.auth.service;

import com.pixelpro.auth.entity.UserEntity;
import com.pixelpro.auth.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional // ¡Importante para evitar LazyInitializationException!
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity u = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        return User.builder()
                .username(u.getEmail())
                .password(u.getPasswordHash())
                // 1. Aquí añadimos el prefijo "ROLE_"
                .authorities(new SimpleGrantedAuthority("ROLE_" + u.getRole().getRoleName().name()))
                // 2. Aquí comprobamos si la cuenta está deshabilitada
                .disabled(!u.isEnabled())
                .build();
    }
}