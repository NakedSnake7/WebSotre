package com.WeedTitlan.server;
import com.WeedTitlan.server.exceptions.EmailAlreadyExistsException;
import com.WeedTitlan.server.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void saveUser_success() {
        // Mockeamos las dependencias necesarias
        UserRepository mockRepository = Mockito.mock(UserRepository.class);

        // Creamos la instancia de UserService pasando el repositorio
        UserService userService = new UserService(mockRepository);

        // Creamos un usuario de prueba
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");

        // Simulamos el comportamiento del repositorio
        Mockito.when(mockRepository.existsByEmail("test@example.com"))
               .thenReturn(false); // No existe el usuario en la base de datos
        Mockito.when(mockRepository.save(user)).thenReturn(user); // Simulamos el guardado del usuario

        // Llamamos al método de servicio
        User savedUser = userService.saveUser(user);

        // Verificamos que el usuario se haya guardado correctamente
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Test User", savedUser.getName());
    }

    @Test
    void saveUser_emailAlreadyExists() {
        // Mockeamos las dependencias necesarias
        UserRepository mockRepository = Mockito.mock(UserRepository.class);

        // Creamos la instancia de UserService pasando el repositorio
        UserService userService = new UserService(mockRepository);

        // Creamos un usuario de prueba
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");

        // Simulamos que el correo ya existe en la base de datos
        Mockito.when(mockRepository.existsByEmail("test@example.com"))
               .thenReturn(true);

        // Verificamos que se lanza la excepción esperada
        Exception exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.saveUser(user)
        );

        assertEquals("El correo electrónico ya está registrado.", exception.getMessage());
    }
}
