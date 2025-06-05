package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.ImagenProducto; 
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.service.ProductoService;
import com.WeedTitlan.server.service.ImagenProductoService;
import com.WeedTitlan.server.service.CloudinaryService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private CloudinaryService cloudinaryService;

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

            String urlCloudinary = cloudinaryService.subirImagen(file);

            ImagenProducto imagen = new ImagenProducto();
            imagen.setImageUrl(urlCloudinary);
            imagen.setProducto(producto);
            imagenProductoService.guardarImagen(imagen);

            return ResponseEntity.ok("Imagen subida con éxito: " + urlCloudinary);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir la imagen");
        }
    }

    @PostMapping("/subirProducto")
    public ResponseEntity<String> subirProducto(
        @RequestParam("productName") String productName,
        @RequestParam("price") double price,
        @RequestParam("stock") int stock,
        @RequestParam("category") String category,
        @RequestParam("description") String description,
        @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes) {

        try {
            if (productName.isEmpty() || category.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El nombre y la categoría son obligatorios.");
            }

            // 1. Guardar el producto sin imágenes aún
            Producto nuevoProducto = new Producto();
            nuevoProducto.setProductName(productName);
            nuevoProducto.setPrice(price);
            nuevoProducto.setStock(stock);
            nuevoProducto.setCategory(category);
            nuevoProducto.setDescription(description);

            Producto productoGuardado = productoService.guardarProducto(nuevoProducto);

            // 2. Subir múltiples imágenes si se proporcionan
            if (imagenes != null && imagenes.length > 0) {
                int imagenesSubidas = 0;

                for (MultipartFile imagen : imagenes) {
                    if (imagen != null && !imagen.isEmpty()) {
                        String urlImagen = cloudinaryService.subirImagen(imagen);
                        ImagenProducto imagenProducto = new ImagenProducto();
                        imagenProducto.setImageUrl(urlImagen);
                        imagenProducto.setProducto(productoGuardado);
                        imagenProductoService.guardarImagen(imagenProducto);
                        imagenesSubidas++;
                    }
                }

                return ResponseEntity.ok("Producto y " + imagenesSubidas + " imagen(es) subidas correctamente.");
            }

            return ResponseEntity.ok("Producto subido correctamente sin imágenes.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir el producto.");
        }
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

    @PostMapping("/actualizar")
    public String actualizarProducto(
        @ModelAttribute Producto producto, 
        @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes) {

        Producto productoExistente = productoService.obtenerProducto(producto.getId());

        if (productoExistente == null) {
            return "redirect:/VerProductos?error=ProductoNoEncontrado";
        }

        try {
            // Si se suben nuevas imágenes, eliminar las anteriores y guardar las nuevas
            if (imagenes != null && imagenes.length > 0) {
                // Eliminar imágenes anteriores
                imagenProductoService.eliminarImagenesPorProducto(productoExistente.getId());
                
                for (MultipartFile imagen : imagenes) {
                    if (imagen != null && !imagen.isEmpty()) {
                        String urlCloudinary = cloudinaryService.subirImagen(imagen);

                        ImagenProducto nuevaImagen = new ImagenProducto();
                        nuevaImagen.setImageUrl(urlCloudinary);
                        nuevaImagen.setProducto(productoExistente);
                        imagenProductoService.guardarImagen(nuevaImagen);
                    }
                }
            }

            // Actualizar los demás campos del producto
            productoExistente.setProductName(producto.getProductName());
            productoExistente.setPrice(producto.getPrice());
            productoExistente.setStock(producto.getStock());
            productoExistente.setCategory(producto.getCategory());
            productoExistente.setDescription(producto.getDescription());

            productoService.guardarProducto(productoExistente);

            return "redirect:/VerProductos?success=ProductoActualizado";
        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/VerProductos?error=ErrorAlGuardarImagen";
        }
    }
}