package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.Categoria;
import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.repository.CategoriaRepository;
import com.WeedTitlan.server.service.CategoriaService;
import com.WeedTitlan.server.service.CloudinaryService;
import com.WeedTitlan.server.service.ImagenProductoService;
import com.WeedTitlan.server.service.ProductoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final ImagenProductoService imagenProductoService;
    private final CloudinaryService cloudinaryService;
    private final CategoriaRepository categoriaRepository;
    private final CategoriaService categoriaService;

    public ProductoController(ProductoService productoService,
                              ImagenProductoService imagenProductoService,
                              CloudinaryService cloudinaryService,
                              CategoriaRepository categoriaRepository,
                              CategoriaService categoriaService) {
        this.productoService = productoService;
        this.imagenProductoService = imagenProductoService;
        this.cloudinaryService = cloudinaryService;
        this.categoriaRepository = categoriaRepository;
        this.categoriaService = categoriaService;
    }

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

            if (file.isEmpty() || !file.getContentType().startsWith("image/")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El archivo no es una imagen válida");
            }

            String urlCloudinary = cloudinaryService.subirImagen(file);
            ImagenProducto imagen = new ImagenProducto(urlCloudinary, producto);
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
            Categoria categoria = obtenerOCrearCategoria(nuevaCategoria, categoriaIdStr);
            if (categoria == null) return "redirect:/VerProductos?error=CategoriaNoValida";

            Producto nuevoProducto = new Producto(productName, price, stock, description, categoria);
            Producto productoGuardado = productoService.guardarProducto(nuevoProducto);

            if (imagenes != null) {
                for (MultipartFile imagen : imagenes) {
                    if (!imagen.isEmpty()) {
                        String urlImagen = cloudinaryService.subirImagen(imagen);
                        imagenProductoService.guardarImagen(new ImagenProducto(urlImagen, productoGuardado));
                    }
                }
            }
            return "redirect:/VerProductos?success=subido";
        } catch (Exception e) {
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
            @RequestParam(value = "nuevasImagenes", required = false) MultipartFile[] imagenes,
            @RequestParam(value = "eliminarImagenes", required = false) List<Long> imagenesAEliminar,
            @RequestParam(value = "nuevaCategoria", required = false) String nuevaCategoria,
            @RequestParam(value = "categoriaId", required = false) Long categoriaId) {

        Producto productoExistente = productoService.obtenerProducto(producto.getId());
        if (productoExistente == null) return "redirect:/VerProductos?error=ProductoNoEncontrado";

        try {
            Categoria categoria = obtenerOCrearCategoria(nuevaCategoria, categoriaId);
            if (categoria == null) return "redirect:/VerProductos?error=CategoriaNoValida";

            if (imagenesAEliminar != null) {
                for (Long imagenId : imagenesAEliminar) {
                    imagenProductoService.eliminarPorId(imagenId);
                }
            }

            if (imagenes != null) {
                for (MultipartFile imagen : imagenes) {
                    if (!imagen.isEmpty()) {
                        String url = cloudinaryService.subirImagen(imagen);
                        imagenProductoService.guardarImagen(new ImagenProducto(url, productoExistente));
                    }
                }
            }

            productoExistente.actualizarDatosDesde(producto, categoria);
            productoService.guardarProducto(productoExistente);

            return "redirect:/VerProductos?success=ProductoActualizado";
        } catch (IOException e) {
            return "redirect:/VerProductos?error=ErrorAlGuardarImagen";
        }
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable("id") Long id, Model model) {
    	Producto producto = productoService.obtenerPorIdConCategoria(id);
        List<Categoria> categorias = categoriaService.obtenerTodas();
        System.out.println("Categorías encontradas: " + categorias.size());
        model.addAttribute("producto", producto);
        model.addAttribute("categorias", categorias);
        System.out.println("Producto: " + producto.getProductName());
        System.out.println("Categorías disponibles:");
        for (Categoria c : categorias) {
            System.out.println("- " + c.getId() + ": " + c.getNombre());
        }
        System.out.println("✔ Producto: " + producto.getProductName());
        System.out.println("✔ Categorías en model: " + categorias);
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

    // Helpers
    private Categoria obtenerOCrearCategoria(String nuevaCategoria, Object categoriaIdObj) {
        if (nuevaCategoria != null && !nuevaCategoria.trim().isEmpty()) {
            return categoriaRepository.findByNombre(nuevaCategoria.trim()) != null ?
                    categoriaRepository.findByNombre(nuevaCategoria.trim()) :
                    categoriaRepository.save(new Categoria(nuevaCategoria.trim()));
        } else if (categoriaIdObj != null) {
            Long categoriaId = (categoriaIdObj instanceof Long) ? (Long) categoriaIdObj : Long.parseLong(categoriaIdObj.toString());
            return categoriaRepository.findById(categoriaId).orElse(null);
        }
        return null;
    }
} 
