package com.WeedTitlan.server.controller;

import java.util.List; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.service.ProductoService;

@Controller
@RequestMapping("/") // Ruta base para las vistas
public class ProductoViewController {

    @Autowired
    private ProductoService productoService;

 
    @GetMapping("/VerProductos")
    public String mostrarSubirProducto(Model model) {
        List<Producto> productos = productoService.obtenerTodosLosProductos();
        model.addAttribute("productos", productos);
        return "VerProductos"; // Asegúrate de que el nombre coincide con tu archivo HTML
    }

}
