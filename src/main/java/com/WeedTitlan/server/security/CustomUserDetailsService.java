package com.WeedTitlan.server.security;

import com.WeedTitlan.server.User;
import com.WeedTitlan.server.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar el usuario por email (username)
        User user = userService.findUserByEmail(username); 

        // Crear y devolver el objeto UserDetails
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())  // El email es el nombre de usuario
                .password(user.getPassword())  // La contraseña ya está codificada
                .roles("USER")  // Aquí puedes añadir roles según corresponda
                .build();
    }
}
