package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Categoria findByNombre(String nombre); // por si necesitas buscarla por nombre
}
