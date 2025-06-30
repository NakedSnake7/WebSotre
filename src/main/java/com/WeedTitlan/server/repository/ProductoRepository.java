package com.WeedTitlan.server.repository;

//import com.WeedTitlan.server.dto.ProductoResumenDTO;
import com.WeedTitlan.server.model.Producto; 
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.imagenes")
    List<Producto> findAllWithImages();
    // Método para buscar por nombre del producto
    Optional<Producto> findByProductName(String productName);
    
    @EntityGraph(attributePaths = {"categoria"})
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdWithCategoria(@Param("id") Long id);
  


    @Query("SELECT DISTINCT p.categoria.nombre FROM Producto p WHERE p.visibleEnMenu = true")
    List<String> obtenerNombresCategoriasVisibles();
    @EntityGraph(attributePaths = {"imagenes", "categoria"})
    @Query("SELECT p FROM Producto p WHERE p.visibleEnMenu = true")
    List<Producto> findProductosVisiblesConTodo();


}
