package com.WeedTitlan.server.service;

import org.springframework.security.core.userdetails.User; 
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Esta clase debe estar marcada con @Service para que Spring la registre como un Bean
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // Método que se utiliza para cargar los detalles de un usuario desde la base de datos
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Este es un ejemplo estático, aquí puedes acceder a tu base de datos para obtener al usuario
        if ("admin".equals(username)) {
            return User.builder()
                    .username("admin")
                    .password("{bcrypt}$2a$10$nwPg/2Wu9Kia9SC24YlRHuElooBoBvnhhrcVEGQbj2gV6qv9FuSri")
                    .roles("ADMIN")
                    .build();
        }
        throw new UsernameNotFoundException("Usuario no encontrado");
    }
}
