package com.WeedTitlan.server;

import com.WeedTitlan.server.repository.UserRepository; 
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.WeedTitlan.server.exceptions.UserNotFoundException;
import com.WeedTitlan.server.exceptions.EmailAlreadyExistsException;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository; 
    private final PasswordEncoder passwordEncoder;

    // Constructor con ambas dependencias
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Verifica si el email ya existe en la base de datos
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Guardar un usuario
    @Transactional  
    public User saveUser(User user) {
        // Verificar si el email ya está registrado
        if (existsByEmail(user.getEmail())) {
            throw new EmailAlreadyExistsException("El correo electrónico ya está registrado.");
        }

        // Codificar la contraseña antes de guardar el usuario
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Buscar un usuario por su correo electrónico
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Buscar un usuario por correo o lanzar una excepción si no existe
    public User findUserByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con el email: " + email));
    }
}
