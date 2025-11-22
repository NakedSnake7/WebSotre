package com.WeedTitlan.server.service;

import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.repository.ImagenProductoRepository;

import java.util.List;


import org.springframework.stereotype.Service;

@Service
public class ImagenProductoService {

    private final ImagenProductoRepository imagenProductoRepository;
    private final CloudinaryService cloudinaryService;
    
    public ImagenProducto obtenerPorId(Long id) {
        return imagenProductoRepository.findById(id).orElse(null);
    }


    public void eliminarPorId(Long id) {
        ImagenProducto imagen = imagenProductoRepository.findById(id).orElse(null);
        if (imagen != null) {
            cloudinaryService.eliminarImagen(imagen.getPublicId()); // eliminar en Cloudinary
            imagenProductoRepository.deleteById(id);                // eliminar en DB
        }
    }


 
    public ImagenProductoService(ImagenProductoRepository imagenProductoRepository,
                                 CloudinaryService cloudinaryService) {
        this.imagenProductoRepository = imagenProductoRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public void guardarImagen(ImagenProducto imagen) {
        imagenProductoRepository.save(imagen);
    }

    public void eliminarImagenesPorProducto(Long productoId) {
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoId(productoId);
        for (ImagenProducto imagen : imagenes) {
            if (imagen.getPublicId() != null) {
                cloudinaryService.eliminarImagen(imagen.getPublicId());
            }
        }
        imagenProductoRepository.deleteAll(imagenes);
    }

    public void eliminarImagen(ImagenProducto imagenProducto) {
        if (imagenProducto.getPublicId() != null) {
            cloudinaryService.eliminarImagen(imagenProducto.getPublicId());
        }
        imagenProductoRepository.delete(imagenProducto);
    }

}
