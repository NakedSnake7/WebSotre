package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.service.ProductoService;
import com.WeedTitlan.server.service.ImagenProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private static final String IMAGE_UPLOAD_DIR = "/imgs/";

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ImagenProductoService imagenProductoService;

    @PutMapping("/{id}/stock/{cantidad}")
    public ResponseEntity<String> actualizarStock(@PathVariable Long id, @PathVariable int cantidad) {
        try {
            productoService.actualizarStock(id, cantidad);
            return ResponseEntity.ok("Stock actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/subirImagen")
    public ResponseEntity<String> subirImagen(@PathVariable Long id, @RequestParam("imagen") MultipartFile file) {
        try {
            Producto producto = productoService.obtenerProducto(id);
            if (producto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
            }
            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo está vacío");
            }

            String fileName = guardarImagen(file);
            ImagenProducto imagen = new ImagenProducto();
            imagen.setImageUrl(fileName);
            imagen.setProducto(producto);
            imagenProductoService.guardarImagen(imagen);

            return ResponseEntity.ok("Imagen subida con éxito: " + fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar la imagen");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/subirProducto")
    public ResponseEntity<String> subirProducto(
        @RequestParam("productName") String productName,
        @RequestParam("price") double price,
        @RequestParam("stock") int stock,
        @RequestParam("category") String category,
        @RequestParam("description") String description,
        @RequestParam(value = "imagen", required = false) MultipartFile imagen) {

        try {
            if (productName.isEmpty() || category.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre y la categoría son obligatorios.");
            }

            Producto nuevoProducto = new Producto();
            nuevoProducto.setProductName(productName);
            nuevoProducto.setPrice(price);
            nuevoProducto.setStock(stock);
            nuevoProducto.setCategory(category);
            nuevoProducto.setDescription(description);

            Producto productoGuardado = productoService.guardarProducto(nuevoProducto);

            // Subir imagen si se proporcionó
            if (imagen != null && !imagen.isEmpty()) {
                String mensajeImagen = subirImagenProducto(productoGuardado.getId(), imagen);
                return ResponseEntity.ok("Producto y imagen subidos correctamente. " + mensajeImagen);
            }

            return ResponseEntity.ok("Producto subido correctamente sin imagen.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el producto.");
        }
    }

    private String subirImagenProducto(Long productoId, MultipartFile imagen) {
        try {
            String fileName = guardarImagen(imagen);
            ImagenProducto imagenProducto = new ImagenProducto();
            imagenProducto.setImageUrl("/imgs/" + fileName);
            imagenProducto.setProducto(productoService.obtenerProducto(productoId));
            imagenProductoService.guardarImagen(imagenProducto);
            return "Imagen subida con éxito: " + fileName;
        } catch (IOException e) {
            return "Error al guardar la imagen: " + e.getMessage();
        }
    }

    private String guardarImagen(MultipartFile imagen) throws IOException {
        // Generar un nombre único para la imagen utilizando UUID
        String fileName = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
        Path path = Paths.get(IMAGE_UPLOAD_DIR + fileName);

        // Guardar la imagen en la carpeta del servidor
        Files.write(path, imagen.getBytes());

        return fileName;
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok("Producto eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
