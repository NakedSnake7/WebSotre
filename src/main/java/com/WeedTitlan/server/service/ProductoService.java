package com.WeedTitlan.server.service;

import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.repository.ImagenProductoRepository;
import com.WeedTitlan.server.repository.ProductoRepository;
import com.WeedTitlan.server.dto.CloudinaryUploadResult;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ImagenProductoRepository imagenProductoRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    // --------------------------------------------------------------------
    // LISTADOS
    // --------------------------------------------------------------------

    public List<Producto> obtenerTodosLosProductos() {
        return productoRepository.findAllConTodo();
    }


    @Transactional
    public List<Producto> listarProductos() {
        return productoRepository.findAllConTodo();
    }

    public List<String> obtenerCategorias() {
        return productoRepository.obtenerNombresCategoriasVisibles();
    }

    public List<Producto> obtenerProductosVisiblesConTodo() {
        return productoRepository.findProductosVisiblesConTodo();
    }

    public Producto obtenerProductoConTodo(Long id) {
        return productoRepository.findByIdConTodo(id).orElse(null);
    }

    // --------------------------------------------------------------------
    // OBTENCIONES POR ID
    // --------------------------------------------------------------------

    @Transactional
    public Producto obtenerProducto(Long id) {
        return productoRepository.findByIdConTodo(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    @Transactional
    public Producto obtenerProductoConCategoria(Long id) {
        return productoRepository.findByIdWithCategoria(id)
                .orElseThrow(() -> new ProductoNotFoundException(id));
    }

    public Optional<Producto> buscarPorNombre(String nombre) {
        return productoRepository.findByProductName(nombre);
    }

    // --------------------------------------------------------------------
    // CRUD
    // --------------------------------------------------------------------

    @Transactional
    public Producto guardarProducto(Producto producto) {
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public Producto actualizarProducto(Producto producto) {
        if (!productoRepository.existsById(producto.getId())) {
            throw new ProductoNotFoundException(producto.getId());
        }
        validarProducto(producto);
        return productoRepository.save(producto);
    }

    @Transactional
    public void actualizarCamposBasicos(Producto datos) {
        Producto existente = obtenerProducto(datos.getId());

        existente.setProductName(datos.getProductName());
        existente.setPrice(datos.getPrice());
        existente.setStock(datos.getStock());
        existente.setDescription(datos.getDescription());

        validarProducto(existente);
        productoRepository.save(existente);
    }

    @Transactional
    public void actualizarStock(Long id, int cantidad) {
        if (cantidad < 0) throw new IllegalArgumentException("Cantidad no puede ser negativa");

        Producto producto = obtenerProducto(id);
        if (producto.getStock() < cantidad) {
            throw new RuntimeException("Stock insuficiente");
        }

        producto.setStock(producto.getStock() - cantidad);
        productoRepository.save(producto);
    }
    
    
    @Transactional
    public boolean togglePromocion(Long id) {
        Producto producto = obtenerProducto(id); // usa tu método ya existente

        boolean nuevoEstado = !Boolean.TRUE.equals(producto.getTienePromocion());
        producto.setTienePromocion(nuevoEstado);

        productoRepository.save(producto);
        return nuevoEstado;
    }

    

    @Transactional
    public void eliminarProducto(Long id) {
        Producto producto = obtenerProducto(id);

        // Eliminar imágenes en Cloudinary
        eliminarImagenesPorProducto(producto);

        productoRepository.delete(producto);
    }

    // --------------------------------------------------------------------
    // IMÁGENES
    // --------------------------------------------------------------------
    @Transactional
    public boolean eliminarImagenPorIdInmediato(Long idImagen) {
        ImagenProducto img = imagenProductoRepository.findById(idImagen).orElse(null);
        if (img == null) return false;

        // 1. Eliminar de Cloudinary
        boolean cloudDeleted = cloudinaryService.eliminarImagen(img.getPublicId());
        if (!cloudDeleted) {
            System.out.println("⚠ No se pudo eliminar en Cloudinary: " + img.getPublicId());
        }

        // 2. Quitar referencia del Producto (MUY IMPORTANTE)
        Producto producto = img.getProducto();
        if (producto != null) {
            producto.getImagenes().remove(img);
        }

        // 3. Eliminar de BD
        imagenProductoRepository.delete(img);

        return true;
    }

    @Transactional
    public List<ImagenProducto> subirImagenes(Producto producto, List<MultipartFile> nuevasImagenes) throws IOException {
        List<ImagenProducto> imagenesAgregadas = new ArrayList<>();

        // Tipos permitidos
        List<String> tiposPermitidos = List.of("image/jpeg", "image/png", "image/webp");

        if (nuevasImagenes != null) {
            for (MultipartFile archivo : nuevasImagenes) {
                if (!archivo.isEmpty()) {

                    // ==== VALIDACIÓN DE TIPO DE ARCHIVO ====
                    String contentType = archivo.getContentType();

                    if (contentType == null || !tiposPermitidos.contains(contentType)) {
                        throw new IllegalArgumentException(
                                "Formato no permitido. Solo se permiten imágenes JPG, PNG o WebP."
                        );
                    }

                    // ==== SUBIR A CLOUDINARY ====
                    CloudinaryUploadResult result = cloudinaryService.subirImagen(archivo);

                    ImagenProducto imagen = new ImagenProducto(
                            result.getSecureUrl(),
                            result.getPublicId(),
                            producto
                    );

                    imagenProductoRepository.save(imagen);
                    imagenesAgregadas.add(imagen);
                }
            }
        }

        return imagenesAgregadas;
    }



    @Transactional
    public void eliminarImagenesPorProducto(Producto producto) {
        List<ImagenProducto> imagenes = imagenProductoRepository.findByProductoId(producto.getId());
        for (ImagenProducto img : imagenes) {
            cloudinaryService.eliminarImagen(img.getPublicId());
        }
        imagenProductoRepository.deleteAll(imagenes);
    }

    @Transactional
    public void eliminarImagenesPorIds(List<Long> ids) {

        if (ids == null || ids.isEmpty()) return;

        // 1 sola query para traer TODAS las imágenes
        List<ImagenProducto> imagenes = imagenProductoRepository.findAllById(ids);

        // Borrar de Cloudinary primero
        for (ImagenProducto img : imagenes) {
            cloudinaryService.eliminarImagen(img.getPublicId());
        }

        // 1 sola query para eliminar TODAS en lote
        imagenProductoRepository.deleteAllInBatch(imagenes);
    }

    // --------------------------------------------------------------------
    // VALIDACIONES
    // --------------------------------------------------------------------

    private void validarProducto(Producto producto) {
        if (producto.getProductName() == null || producto.getProductName().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto no puede estar vacío");
        }
        if (producto.getPrice() < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }

    // --------------------------------------------------------------------
    // EXCEPCIONES PERSONALIZADAS
    // --------------------------------------------------------------------

    public static class ProductoNotFoundException extends RuntimeException {
        private static final long serialVersionUID = 1L; 
        public ProductoNotFoundException(Long id) {
            super("Producto no encontrado con ID: " + id);
        }
    }

}
