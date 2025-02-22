package com.WeedTitlan.server.controller;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.service.ProductoService;

@Controller
@RequestMapping("/productos") // Ruta base para las vistas
public class ProductoViewController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    public String mostrarProductos(Model model) {
        List<Producto> productos = productoService.listarProductos(); // Asegúrate de tener este método en tu servicio
        Set<String> categorias = Set.of("carts", "comestibles", "otros"); 

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);

        return "index"; // Nombre de la plantilla Thymeleaf
    }
}
