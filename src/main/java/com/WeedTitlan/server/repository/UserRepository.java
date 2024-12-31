package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.User; 
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Este método generará la consulta de forma automática gracias a Spring Data JPA
    Optional<User> findByEmail(String email);

    // Puedes agregar más métodos personalizados aquí si los necesitas
    boolean existsByEmail(String email);
}