package com.WeedTitlan.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index", "/inicio", "/productos/**", "/css/**", "/js/**", "/images/**").permitAll() // Rutas públicas
                .requestMatchers("/subirProducto", "/VerProductos", "/servicio").authenticated()
                .anyRequest().permitAll() // Todo lo demás es accesible
            )
            .formLogin(form -> form
                .loginPage("/login") // Página personalizada de login
                .defaultSuccessUrl("/subirProducto", false) // Redirige aquí tras login
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/index") 
                .permitAll()
            );

        return http.build();
    }
}
