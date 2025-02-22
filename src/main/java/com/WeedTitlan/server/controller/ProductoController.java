package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.ImagenProducto;   
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.service.ProductoService;



import com.WeedTitlan.server.service.ImagenProductoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    private static final String IMAGE_UPLOAD_DIR = "src/main/resources/static/imgs/";

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ImagenProductoService imagenProductoService;

    @GetMapping("/subirProducto")
    public String mostrarSubirProducto(Model model) {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        model.addAttribute("productos", productos);
        return "subirProducto"; // Asegúrate de que el nombre coincide con tu archivo HTML
    }

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
            // Obtener el producto
            Producto producto = productoService.obtenerProducto(id);

            if (producto == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado");
            }

            if (file.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo está vacío");
            }

            // Generar un nombre único para la imagen
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(IMAGE_UPLOAD_DIR + fileName);

            // Guardar la imagen en la carpeta del servidor
            Files.write(path, file.getBytes());

            // Crear la entidad de la imagen y asociarla al producto
            ImagenProducto imagen = new ImagenProducto();
            imagen.setImageUrl(fileName); // Asegúrate de que 'imageUrl' existe en ImagenProducto
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
        @RequestParam("imagen") MultipartFile imagen) {

        try {
            // Validaciones de entrada
            if (productName.isEmpty() || category.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre y la categoría son obligatorios.");
            }

            // Crea un nuevo producto con los datos recibidos
            Producto nuevoProducto = new Producto();
            nuevoProducto.setProductName(productName);
            nuevoProducto.setPrice(price);
            nuevoProducto.setStock(stock);
            nuevoProducto.setCategory(category);
            nuevoProducto.setDescription(description);

            // Guardar el producto en la base de datos
            Producto productoGuardado = productoService.guardarProducto(nuevoProducto);

            // Guardar la imagen asociada al producto
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
            // Generar un nombre único para la imagen
            String fileName = System.currentTimeMillis() + "_" + imagen.getOriginalFilename();
            Path path = Paths.get(IMAGE_UPLOAD_DIR + fileName);

            // Guardar la imagen en la carpeta del servidor
            Files.write(path, imagen.getBytes());

            // Crear la entidad de la imagen y asociarla al producto
            ImagenProducto imagenProducto = new ImagenProducto();
            imagenProducto.setImageUrl("/imgs/" + fileName);  // Guarda la URL relativa
            imagenProducto.setProducto(productoService.obtenerProducto(productoId));
            imagenProductoService.guardarImagen(imagenProducto);

            return "Imagen subida con éxito: " + fileName;
        } catch (IOException e) {
            return "Error al guardar la imagen: " + e.getMessage();
        }
    }
    // Endpoint para eliminar un producto
    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.ok("Producto eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}
