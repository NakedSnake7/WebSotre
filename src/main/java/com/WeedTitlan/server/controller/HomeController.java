package com.WeedTitlan.server.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model; // Importa el Model correcto

@Controller
public class HomeController {

    @GetMapping({"/","/inicio"}) // Agrega más rutas para evitar problemas
    public String home(Model model) {
        return "index";  // Esto busca index.html en 'templates' si usas Thymeleaf
    }
    @GetMapping("/subirProducto")
    public String subirProducto(Model model, @AuthenticationPrincipal UserDetails user) {
        if (user == null) {
            return "redirect:/login"; // Si no está autenticado, redirige al login
        }
        return "subirProducto";  // Devuelve la vista protegida
    }
}