package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.ProductoDTO;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.service.ProductoService;
import com.WeedTitlan.server.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired private ProductoService productoService;
    @Autowired private CategoriaService categoriaService;

    @GetMapping
    public List<Producto> listarProductos() {
        return productoService.obtenerTodosLosProductos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productoService.obtenerProducto(id));
        } catch (ProductoService.ProductoNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            productoService.eliminarProducto(id);
            return ResponseEntity.noContent().build();
        } catch (ProductoService.ProductoNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/editar/{id}")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id,
            @Valid @ModelAttribute ProductoDTO dto,
            @RequestParam(value = "imagenes", required = false) List<MultipartFile> nuevasImagenes,
            @RequestParam(value = "eliminarImagenes", required = false) List<Long> eliminarImagenes,
            @RequestParam(value = "porcentajeDescuento", defaultValue = "0") double porcentajeDescuento) {

        try {

            // Obtener el producto
            Producto producto = productoService.obtenerProducto(id);

            // === DATOS BÁSICOS ===
            producto.setCategoria(
                    categoriaService.obtenerOCrearCategoria(dto.getCategoriaNombre())
            );
            producto.setProductName(dto.getProductName());
            producto.setPrice(dto.getPrice());
            producto.setDescription(dto.getDescription());
            producto.setPorcentajeDescuento((double) porcentajeDescuento);

            // === SUBIR NUEVAS IMÁGENES (si vienen) ===
            List<ImagenProducto> agregadas = List.of(); // por si es null

            if (nuevasImagenes != null && !nuevasImagenes.isEmpty()) {
                agregadas = productoService.subirImagenes(producto, nuevasImagenes);
            }

            // === ELIMINAR IMÁGENES SELECCIONADAS (si vienen) ===
            if (eliminarImagenes != null && !eliminarImagenes.isEmpty()) {
                productoService.eliminarImagenesPorIds(eliminarImagenes);
            }

            // === GUARDAR CAMBIOS ===
            productoService.guardarProducto(producto);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "id", producto.getId(),
                    "imagenesAgregadas", agregadas.size(),
                    "imagenesEliminadas", eliminarImagenes == null ? 0 : eliminarImagenes.size(),
                    "message", "Producto actualizado correctamente"
            ));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error subiendo imágenes"));
        } catch (ProductoService.ProductoNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/eliminar-imagen/{idImagen}")
    public ResponseEntity<?> eliminarImagenInmediato(@PathVariable Long idImagen) {
        boolean ok = productoService.eliminarImagenPorIdInmediato(idImagen);

        return ok
                ? ResponseEntity.noContent().build()
                : ResponseEntity.status(404).body(Map.of("error", "Imagen no encontrada"));
    }
    
    @PostMapping("/togglePromocion")
    public ResponseEntity<Boolean> togglePromocion(@RequestParam Long productoId) {
        boolean enPromocion = productoService.togglePromocion(productoId);
        return ResponseEntity.ok(enPromocion);
    }
    @PostMapping("/toggleVisibility")
    public ResponseEntity<Boolean> toggleVisibility(@RequestParam Long productoId) {
        boolean visible = productoService.toggleVisibility(productoId);
        return ResponseEntity.ok(visible);
    }





}
