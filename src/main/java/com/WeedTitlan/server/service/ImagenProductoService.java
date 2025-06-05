package com.WeedTitlan.server.service;

import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.repository.ImagenProductoRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ImagenProductoService {

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    public void guardarImagen(ImagenProducto imagen) {
        imagenProductoRepository.save(imagen);
    }
    public void eliminarImagenesPorProducto(Long productoId) {
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoId(productoId);
        imagenProductoRepository.deleteAll(imagenes);
    }

}
