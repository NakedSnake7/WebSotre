package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.service.ProductoService;
import com.WeedTitlan.server.service.ImagenProductoService;
import com.WeedTitlan.server.service.CloudinaryService;
import com.WeedTitlan.server.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import java.util.List;


import java.io.IOException;
import com.WeedTitlan.server.model.Categoria;
import com.WeedTitlan.server.repository.CategoriaRepository;


@Controller
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ImagenProductoService imagenProductoService;
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private CategoriaService categoriaService;



    @ResponseBody
    @PutMapping("/{id}/stock/{cantidad}")
    public ResponseEntity<String> actualizarStock(@PathVariable Long id, @PathVariable int cantidad) {
        try {
            productoService.actualizarStock(id, cantidad);
            return ResponseEntity.ok("Stock actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @ResponseBody
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
    public String subirProducto(
            @RequestParam("productName") String productName,
            @RequestParam("price") double price,
            @RequestParam("stock") int stock,
            @RequestParam(value = "categoriaId", required = false) String categoriaIdStr,
            @RequestParam("description") String description,
            @RequestParam(value = "nuevaCategoria", required = false) String nuevaCategoria,
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes) {

        try {
            // Determinar la categoría final
            Categoria categoria = null;

            if (nuevaCategoria != null && !nuevaCategoria.trim().isEmpty()) {
                // Buscar si ya existe una categoría con ese nombre
                categoria = categoriaRepository.findByNombre(nuevaCategoria.trim());
                if (categoria == null) {
                    categoria = new Categoria();
                    categoria.setNombre(nuevaCategoria.trim());
                    categoria = categoriaRepository.save(categoria);
                }
            } 
            else {
                if (categoriaIdStr != null && !categoriaIdStr.trim().isEmpty()) {
                    try {
                        Long categoriaId = Long.parseLong(categoriaIdStr);
                        categoria = categoriaRepository.findById(categoriaId).orElse(null);
                    } catch (NumberFormatException e) {
                        return "redirect:/VerProductos?error=CategoriaNoValida";
                    }
                }
            }


            if (categoria == null) {
                return "redirect:/VerProductos?error=CategoriaNoValida";
            }

            // Crear y guardar el producto
            Producto nuevoProducto = new Producto();
            nuevoProducto.setProductName(productName);
            nuevoProducto.setPrice(price);
            nuevoProducto.setStock(stock);
            nuevoProducto.setCategoria(categoria);
            nuevoProducto.setDescription(description);

            Producto productoGuardado = productoService.guardarProducto(nuevoProducto);

            // Subir imágenes
            if (imagenes != null && imagenes.length > 0) {
                for (MultipartFile imagen : imagenes) {
                    if (!imagen.isEmpty()) {
                        String urlImagen = cloudinaryService.subirImagen(imagen);
                        ImagenProducto imagenProducto = new ImagenProducto();
                        imagenProducto.setImageUrl(urlImagen);
                        imagenProducto.setProducto(productoGuardado);
                        imagenProductoService.guardarImagen(imagenProducto);
                    }
                }
            }

            return "redirect:/VerProductos?success=subido";

        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/VerProductos?error=errorInterno";
        }
    }



    @ResponseBody
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
            @RequestParam(value = "imagenes", required = false) MultipartFile[] imagenes,
            @RequestParam(value = "eliminarImagenes", required = false) List<Long> imagenesAEliminar,
            @RequestParam(value = "nuevaCategoria", required = false) String nuevaCategoria,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId
    ) {
        Producto productoExistente = productoService.obtenerProducto(producto.getId());
        if (productoExistente == null) {
            return "redirect:/VerProductos?error=ProductoNoEncontrado";
        }

        try {
            // Manejo de nueva o existente categoría
            Categoria categoria = null;
            if (nuevaCategoria != null && !nuevaCategoria.trim().isEmpty()) {
                categoria = categoriaRepository.findByNombre(nuevaCategoria.trim());
                if (categoria == null) {
                    categoria = new Categoria();
                    categoria.setNombre(nuevaCategoria.trim());
                    categoria = categoriaRepository.save(categoria);
                }
            } else if (categoriaId != null) {
                categoria = categoriaRepository.findById(categoriaId).orElse(null);
            }

            if (categoria == null) {
                return "redirect:/VerProductos?error=CategoriaNoValida";
            }

            // Eliminar imágenes seleccionadas
            if (imagenesAEliminar != null) {
                for (Long imagenId : imagenesAEliminar) {
                    imagenProductoService.eliminarPorId(imagenId);
                }
            }

            // Subir nuevas imágenes
            if (imagenes != null && imagenes.length > 0) {
                for (MultipartFile imagen : imagenes) {
                    if (imagen != null && !imagen.isEmpty()) {
                        String url = cloudinaryService.subirImagen(imagen);
                        ImagenProducto nuevaImagen = new ImagenProducto();
                        nuevaImagen.setImageUrl(url);
                        nuevaImagen.setProducto(productoExistente);
                        imagenProductoService.guardarImagen(nuevaImagen);
                    }
                }
            }

            // Actualizar producto
            productoExistente.setProductName(producto.getProductName());
            productoExistente.setPrice(producto.getPrice());
            productoExistente.setStock(producto.getStock());
            productoExistente.setCategoria(categoria);
            productoExistente.setDescription(producto.getDescription());

            productoService.guardarProducto(productoExistente);

            return "redirect:/VerProductos?success=ProductoActualizado";

        } catch (IOException e) {
            e.printStackTrace();
            return "redirect:/VerProductos?error=ErrorAlGuardarImagen";
        }
    }


    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable Long id, Model model) {
        Producto producto = productoService.obtenerPorId(id);
        List<Categoria> categorias = categoriaService.obtenerTodas();
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categorias);
        return "EditarProducto";
    }
    @GetMapping("/subirProducto")
    public String mostrarFormulario(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        return "subirProducto";
    }

    @ResponseBody
    @PostMapping("/toggle-visibility")
    public ResponseEntity<String> toggleVisibility(@RequestParam Long id, @RequestParam boolean visibleEnMenu) {
        Producto producto = productoService.obtenerPorId(id);
        if (producto != null) {
            producto.setVisibleEnMenu(visibleEnMenu);
            productoService.save(producto);
            return ResponseEntity.ok("Visibilidad actualizada correctamente.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Producto no encontrado.");
        }
    }

}
