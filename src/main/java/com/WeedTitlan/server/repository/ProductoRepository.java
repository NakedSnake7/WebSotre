package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.model.Producto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

  

    Optional<Producto> findByProductName(String productName);
    
    @EntityGraph(attributePaths = {"categoria"})
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdWithCategoria(@Param("id") Long id);

    @Query("SELECT DISTINCT p.categoria.nombre FROM Producto p WHERE p.visibleEnMenu = true")
    List<String> obtenerNombresCategoriasVisibles();

    @EntityGraph(attributePaths = {"imagenes", "categoria"})
    @Query("SELECT DISTINCT p FROM Producto p WHERE p.visibleEnMenu = true")
    List<Producto> findProductosVisiblesConTodo();
    
    @Query("SELECT DISTINCT p FROM Producto p " +
    	       "LEFT JOIN FETCH p.imagenes " +
    	       "LEFT JOIN FETCH p.categoria " +
    	       "WHERE p.id = :id")
    Optional<Producto> findByIdConTodo(@Param("id") Long id);
    
    @EntityGraph(attributePaths = {"categoria", "imagenes"})
    @Query("SELECT DISTINCT p FROM Producto p")
    List<Producto> findAllConTodo();
    
    @Query("SELECT DISTINCT p FROM Producto p " +
    	       "LEFT JOIN FETCH p.imagenes " +
    	       "LEFT JOIN FETCH p.categoria " +
    	       "WHERE p.productName = :name")
    	Optional<Producto> findByProductNameConTodo(@Param("name") String name);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.id = :id")
    Optional<Producto> findByIdForUpdate(@Param("id") Long id);


}
