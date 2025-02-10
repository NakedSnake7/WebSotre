package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    // Método para buscar por nombre del producto
    Optional<Producto> findByProductName(String productName);
}
