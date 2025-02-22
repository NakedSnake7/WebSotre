package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.model.Producto; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.imagenes")
    List<Producto> findAllWithImages();
    // Método para buscar por nombre del producto
    Optional<Producto> findByProductName(String productName);
}
