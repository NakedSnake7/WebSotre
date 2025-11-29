package com.WeedTitlan.server.controller;
import com.WeedTitlan.server.dto.ProductoDTO;

import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.model.Categoria;
import com.WeedTitlan.server.model.ImagenProducto;
import com.WeedTitlan.server.service.ProductoService;
import com.WeedTitlan.server.service.ProveedorStockService;
import com.WeedTitlan.server.service.CategoriaService;
import com.WeedTitlan.server.service.ImagenProductoService;
import com.WeedTitlan.server.service.CloudinaryService;
import com.WeedTitlan.server.dto.CloudinaryUploadResult;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/")
public class ProductoViewController {

    @Autowired private ProveedorStockService proveedorStockService;
    @Autowired private ProductoService productoService;
    @Autowired private CategoriaService categoriaService;
    @Autowired private ImagenProductoService imagenProductoService;
    @Autowired private CloudinaryService cloudinaryService;

    // LISTA DE PRODUCTOS
    @GetMapping("/VerProductos")
    public String verProductos(Model model) {
        model.addAttribute("productos", productoService.obtenerTodosLosProductos());
        return "VerProductos";
    }
    
    //menu servicio
    @GetMapping("/servicio")
    public String servicio() {
        return "servicio"; // ← nombre del archivo sin .html
    }
    // ========================
    // FORMULARIO NUEVO
    // ========================
    
    
    @GetMapping("/nuevo")
    public String formularioNuevoProducto(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) Integer stock,
            @RequestParam(required = false, name = "categoria") String categoria,
            Model model) {

        ProductoDTO dto = new ProductoDTO();

        if (nombre != null) dto.setProductName(nombre);
        if (stock != null) dto.setStock(stock);
        if (categoria != null) dto.setNuevaCategoria(categoria);


        model.addAttribute("producto", dto);
        model.addAttribute("categorias", categoriaService.obtenerTodas());
        model.addAttribute("categoriaSeleccionada", categoria);

        return "subirProducto";
    }

    // GUARDAR NUEVO
    @PostMapping("/nuevo")
    public String guardarProducto(
            @Valid @ModelAttribute("producto") ProductoDTO productoDTO,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false) List<MultipartFile> imagenes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.obtenerTodas());
            return "subirProducto";
        }

        String categoriaFinal =
                (productoDTO.getNuevaCategoria() != null && !productoDTO.getNuevaCategoria().isBlank())
                        ? productoDTO.getNuevaCategoria()
                        : productoDTO.getCategoriaNombre();

        Categoria categoria = categoriaService.obtenerOCrearCategoria(categoriaFinal);

        Producto producto = new Producto();
        producto.setProductName(productoDTO.getProductName());
        producto.setPrice(productoDTO.getPrice());
        producto.setDescription(productoDTO.getDescription());
        producto.setCategoria(categoria);
        producto.setStock(productoDTO.getStock());

        Producto guardado = productoService.guardarProducto(producto);

        // Imágenes
        if (imagenes != null) {
            for (MultipartFile img : imagenes) {
                if (!img.isEmpty()) {
                    try {
                        CloudinaryUploadResult res = cloudinaryService.subirImagen(img);
                        ImagenProducto imagen = new ImagenProducto(
                                res.getSecureUrl(), res.getPublicId(), guardado
                        );
                        imagenProductoService.guardarImagen(imagen);
                    } catch (IOException e) {
                        model.addAttribute("error", "Error subiendo imagen.");
                        return "subirProducto";
                    }
                }
            }
        }

        return "redirect:/VerProductos";
    }

    // ===========================
    // FORMULARIO EDITAR
    // ===========================
    @GetMapping("/editar/{id}")
    public String formularioEditarProducto(@PathVariable Long id, Model model) {

        Producto producto = productoService.obtenerProductoConTodo(id);
        if (producto == null) return "redirect:/VerProductos";

        ProductoDTO dto = new ProductoDTO();
        dto.setId(producto.getId());
        dto.setProductName(producto.getProductName());
        dto.setPrice(producto.getPrice());
        dto.setDescription(producto.getDescription());
        dto.setCategoriaNombre(producto.getCategoria().getNombre());
        dto.setStock(producto.getStock());
        dto.setPorcentajeDescuento(
                producto.getPorcentajeDescuento() != null 
                ? producto.getPorcentajeDescuento() 
                : 0.0
        );

        dto.setVisibleEnMenu(producto.isVisibleEnMenu());
        dto.setTienePromocion(producto.getTienePromocion());

        // Llenar imágenes existentes
        producto.getImagenes().forEach(img -> {
            dto.getImagenesExistentes().add(img.getId());
            dto.getUrlsImagenesExistentes().add(img.getImageUrl());
        });

        model.addAttribute("productoDTO", dto);
        model.addAttribute("categorias", categoriaService.obtenerTodas());

        return "EditarProducto";
    }


    // GUARDAR EDICIÓN
    @PostMapping("/editar/{id}")
    public String editarProducto(
            @PathVariable Long id,
            @Valid @ModelAttribute("productoDTO") ProductoDTO productoDTO,
            BindingResult result,
            @RequestParam(value = "imagenes", required = false) List<MultipartFile> nuevasImagenes,
            Model model) {

        Producto producto = productoService.obtenerProductoConTodo(id);
        if (producto == null) {
            model.addAttribute("error", "Producto no encontrado.");
            return "EditarProducto";
        }

        if (result.hasErrors()) {
            model.addAttribute("categorias", categoriaService.obtenerTodas());
            return "EditarProducto";
        }

        Categoria categoria = categoriaService.obtenerOCrearCategoria(productoDTO.getCategoriaNombre());

        producto.setProductName(productoDTO.getProductName());
        producto.setPrice(productoDTO.getPrice());
        producto.setDescription(productoDTO.getDescription());
        producto.setCategoria(categoria);
        producto.setStock(productoDTO.getStock());
        producto.setPorcentajeDescuento(productoDTO.getPorcentajeDescuento());
        producto.setVisibleEnMenu(Boolean.TRUE.equals(productoDTO.getVisibleEnMenu()));
        producto.setTienePromocion(Boolean.TRUE.equals(productoDTO.getTienePromocion()));

        // --- ELIMINAR IMÁGENES SELECCIONADAS ---
        List<Long> idsEliminar = new ArrayList<>();
        for (ImagenProducto img : producto.getImagenes()) {
            if (!productoDTO.getImagenesExistentes().contains(img.getId())) {
                idsEliminar.add(img.getId());
            }
        }
        productoService.eliminarImagenesPorIds(idsEliminar);

        // --- SUBIR NUEVAS IMÁGENES ---
        try {
            productoService.subirImagenes(producto, nuevasImagenes);
        } catch (IOException e) {
            model.addAttribute("error", "Error subiendo imágenes.");
            model.addAttribute("categorias", categoriaService.obtenerTodas());
            return "EditarProducto";
        }

        productoService.guardarProducto(producto);
        return "redirect:/VerProductos";
    }



    // IMPORTACIÓN
    @GetMapping("/importar/editar/{nombre}/{stock}")
    public String editarProductoProveedor(
            @PathVariable String nombre,
            @PathVariable int stock,
            Model model) {

        Optional<Producto> optional = productoService.buscarPorNombre(nombre);

        Producto producto = optional.orElseGet(() -> {
            Producto nuevo = new Producto();
            nuevo.setProductName(nombre);
            nuevo.setStock(stock);
            return nuevo;
        });

        producto.setStock(stock);

        model.addAttribute("producto", producto);
        return "editar_producto";
    }

    // STOCK PROVEEDOR
    @GetMapping("/proveedor/stock")
    public String verStockProveedor(Model model) {
        Map<String, List<ProveedorStockService.ProductoStock>> inventario =
                proveedorStockService.obtenerStockPorMarca();

        model.addAttribute("inventarioPorMarca", inventario);
        return "importar_stock";
    }

    @PostMapping("/proveedor/stock/importar")
    public String importarStock(@RequestParam("file") MultipartFile file) {
        try {
            return "redirect:/proveedor/stock?success";
        } catch (Exception e) {
            return "redirect:/proveedor/stock?error";
        }
    }
}
