package com.WeedTitlan.server.service;

import com.WeedTitlan.server.repository.UserRepository;      
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.WeedTitlan.server.exceptions.UserNotFoundException;
import com.WeedTitlan.server.model.User;


import java.util.Optional;
	
@Service
public class UserService {

    private final UserRepository userRepository;

    // Constructor con la dependencia del repositorio
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Verifica si el email ya existe en la base de datos
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    // Guardar un usuario (solo nombre y correo)
    @Transactional  
    public User saveUser(User user) {
        // Verificar si el email ya está registrado
    	 Optional<User> existingUser = userRepository.findByEmail(user.getEmail());
         if (existingUser.isPresent()) {
             return existingUser.get(); // Devuelve el usuario existente
         }
         // Guardar un nuevo usuario si no existe
         return userRepository.save(user);
    }

    // Buscar un usuario por su correo electrónico
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Buscar un usuario por correo o crear uno si no existe
    public User findOrCreateUserByEmail(String email, String name, String phone) {
        Optional<User> existingUser = findByEmail(email);
        if (existingUser.isPresent()) {
            return existingUser.get(); // Usuario ya existe, se devuelve el encontrado
        } else {
            // Si no existe, crear un nuevo usuario
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setFullName(name);// Puedes asignar el nombre o usar uno predeterminado 
            newUser.setPhone(phone != null ? phone : "No disponible");
            return saveUser(newUser); // Guardar el nuevo usuario y devolverlo
        }
    }
    
    // Buscar un usuario por correo o lanzar una excepción si no existe
    public User findUserByEmail(String email) {
        return findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con el email: " + email));
    }
}