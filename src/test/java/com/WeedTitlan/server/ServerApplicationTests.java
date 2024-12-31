package com.WeedTitlan.server;

import com.WeedTitlan.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void saveUser_success() {
        // Mockeamos las dependencias necesarias
        UserRepository mockRepository = Mockito.mock(UserRepository.class);
        PasswordEncoder mockPasswordEncoder = Mockito.mock(PasswordEncoder.class);

        // Creamos la instancia de UserService pasando ambas dependencias
        UserService userService = new UserService(mockRepository, mockPasswordEncoder);

        // Creamos un usuario de prueba
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password123");

        // Simulamos el comportamiento del repositorio y del PasswordEncoder
        Mockito.when(mockRepository.findByEmail("test@example.com"))
               .thenReturn(Optional.empty()); // No existe el usuario en la base de datos
        Mockito.when(mockPasswordEncoder.encode("password123")).thenReturn("encodedPassword"); // Simulamos la codificación de la contraseña
        Mockito.when(mockRepository.save(user)).thenReturn(user); // Simulamos el guardado del usuario

        // Llamamos al método de servicio
        User savedUser = userService.saveUser(user);

        // Verificamos que el usuario se haya guardado correctamente
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword()); // Verificamos que la contraseña fue codificada
    }
}
