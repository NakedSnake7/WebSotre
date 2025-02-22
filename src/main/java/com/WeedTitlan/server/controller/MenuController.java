package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.Producto; 
import com.WeedTitlan.server.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/menu")
public class MenuController {

    @Autowired
    private ProductoRepository productoRepository;

    @GetMapping
    public String mostrarMenu(Model model) {
        List<Producto> productos = productoRepository.findAll();
        
        // Extraer categorías únicas desde los productos
        Set<String> categorias = productos.stream()
            .map(Producto::getCategory)
            .collect(Collectors.toSet());

        System.out.println("Productos: " + productos); // Para depuración
        System.out.println("Categorías: " + categorias);

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categorias);

        return "index"; // Asegúrate de que "index.html" es el nombre correcto de tu vista
    }

}
