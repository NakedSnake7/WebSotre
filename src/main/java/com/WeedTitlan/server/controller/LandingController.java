package com.WeedTitlan.server.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

    @GetMapping("/landing")
    public String mostrarLanding() {
        return "redirect:/landing.html";
    }
}
