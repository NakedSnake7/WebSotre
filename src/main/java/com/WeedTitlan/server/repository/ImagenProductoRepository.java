package com.WeedTitlan.server.repository;

import com.WeedTitlan.server.model.ImagenProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface ImagenProductoRepository extends JpaRepository<ImagenProducto, Long> {
	List<ImagenProducto> findByProductoId(Long productoId);

}
