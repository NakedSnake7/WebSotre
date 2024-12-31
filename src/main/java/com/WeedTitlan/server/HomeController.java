package com.WeedTitlan.server;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";  // Esto redirige a index.html en la carpeta 'static'
    }
}
