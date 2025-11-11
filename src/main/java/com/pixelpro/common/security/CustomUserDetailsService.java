package com.pixelpro.common.security;

// --- CAMBIOS AQUÍ ---
import com.pixelpro.auth.entity.UserEntity;
import com.pixelpro.auth.repository.UserRepository;
// --- FIN DE CAMBIOS ---

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Asumiendo que también tienes un CustomUserDetails en la carpeta 'common/security'
        // Si CustomUserDetails también importa algo de 'iam', ¡tendrás que cambiarlo allí también!
        return new CustomUserDetails(user);
    }
}