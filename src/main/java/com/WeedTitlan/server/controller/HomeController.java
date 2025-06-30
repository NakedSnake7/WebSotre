package com.WeedTitlan.server.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model; // Importa el Model correcto
import com.WeedTitlan.server.service.ProductoService;
//import com.WeedTitlan.server.dto.ProductoResumenDTO;
import com.WeedTitlan.server.model.Producto;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class HomeController {
	@Autowired
	private ProductoService productoService;


	@GetMapping({"/", "/inicio"})
	public String home(Model model) {
	    List<String> categorias = productoService.obtenerCategorias();
	    List<Producto> productos = productoService.obtenerProductosVisiblesConTodo();

	    model.addAttribute("categorias", categorias);
	    model.addAttribute("productos", productos);
	    return "index";
	}

	@GetMapping("/menu")
	public String verMenu(Model model) {
	    List<String> categorias = productoService.obtenerCategorias();
	    List<Producto> productos = productoService.obtenerProductosVisiblesConTodo();

	    model.addAttribute("categorias", categorias);
	    model.addAttribute("productos", productos);
	    model.addAttribute("categorias", categorias);
	    return "index";
	}



    @GetMapping("/subirProducto")
    public String subirProducto(Model model, @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return "redirect:/login"; // Si no está autenticado, redirige al login
        }
        return "subirProducto";  // Devuelve la vista protegida
        
    }
    
    
}