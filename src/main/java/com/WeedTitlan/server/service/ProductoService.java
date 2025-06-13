package com.WeedTitlan.server.service;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.repository.ImagenProductoRepository;
import com.WeedTitlan.server.repository.ProductoRepository;

import jakarta.transaction.Transactional;

@Service
public class ProductoService {

    private static final String IMAGE_UPLOAD_DIR = "src/main/resources/static/assets/imgs/";

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    
    private ImagenProductoRepository imagenProductoRepository;

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAll();
    }

    @Transactional
    public List<Producto> listarProductos() {
        return productoRepository.findAllWithImages();
    }

    @Transactional
    public Producto obtenerProducto(Long id) {
    	return productoRepository.findById(id)
    		    .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

    }

    // ✅ Nuevo método que tu controlador busca: findById
    @Transactional
    public Producto findById(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado")); 
        
    }

    @Transactional
    public Producto obtenerPorId(Long id) {
        return productoRepository.findByIdWithCategoria(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }
    public Producto obtenerPorIdConCategoria(Long id) {
        Producto producto = productoRepository.findByIdWithCategoria(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        System.out.println("✔ Producto: " + producto.getProductName());
        System.out.println("✔ Categoría del producto: " +
            (producto.getCategoria() != null ? producto.getCategoria().getNombre() : "NULL"));

        return producto;
    }



    @Transactional
    public void actualizarStock(Long id, int cantidad) {
        Producto producto = obtenerProducto(id);
        if (producto.getStock() >= cantidad) {
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        } else {
            throw new RuntimeException("Stock insuficiente");
        }
    }

    // ✅ Método usado por el controlador para guardar producto (nombre genérico: save)
    @Transactional
    public Producto save(Producto producto) {
        return productoRepository.save(producto);
    }

    // Método ya existente
    @Transactional
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    // Subida de imagen
    @Transactional
    public String guardarImagen(Long productoId, MultipartFile file) {
        Optional<Producto> productoOpt = productoRepository.findById(productoId);

        if (productoOpt.isEmpty()) {
            throw new RuntimeException("Producto no encontrado");
        }

        if (file.isEmpty()) {
            throw new RuntimeException("El archivo está vacío");
        }

        try {
            Producto producto = productoOpt.get();
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(IMAGE_UPLOAD_DIR + fileName);

            // 🔥 Crear directorio si no existe
            if (!Files.exists(Paths.get(IMAGE_UPLOAD_DIR))) {
                Files.createDirectories(Paths.get(IMAGE_UPLOAD_DIR));
            }

            Files.write(path, file.getBytes());

            ImagenProducto imagen = new ImagenProducto();
            imagen.setImageUrl(fileName);
            imagen.setProducto(producto);
            imagenProductoRepository.save(imagen);

            return "Imagen subida con éxito: " + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen", e);
        }
    }

    public void eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
    }

    public void eliminarImagenesPorProducto(Long productoId) {
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoId(productoId);
        imagenProductoRepository.deleteAll(imagenes);
    }
    @Transactional
    public Producto actualizarProducto(Producto producto) {
        if (!productoRepository.existsById(producto.getId())) {
            throw new RuntimeException("Producto no encontrado con ID: " + producto.getId());
        }
        return productoRepository.save(producto);
    }
    public void actualizarCamposBasicos(Producto nuevoProducto) {
        Producto existente = productoRepository.findById(nuevoProducto.getId())
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        existente.setProductName(nuevoProducto.getProductName());
        existente.setPrice(nuevoProducto.getPrice());
        existente.setStock(nuevoProducto.getStock());
        existente.setDescription(nuevoProducto.getDescription());

        productoRepository.save(existente);
    }

}
