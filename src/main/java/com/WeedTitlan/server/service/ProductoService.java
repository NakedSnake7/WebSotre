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

    // Cargar productos con imágenes usando fetch join
    @Transactional// Asegura que la sesión esté abierta mientras se accede a los datos
    public List<Producto> listarProductos() {
        return productoRepository.findAllWithImages();
    }

    @Transactional
    public Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
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
 // Nuevo método para subir imágenes
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
            Files.write(path, file.getBytes());


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
    @Transactional
    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }
    public void eliminarProducto(Long id) {
        // Verificar si el producto existe
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
        } else {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
    }

}
