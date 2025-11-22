package com.WeedTitlan.server.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.WeedTitlan.server.service.ProductoService;

@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    @Autowired
    private ProductoService productoService;

    @DeleteMapping("/{idImagen}")
    public ResponseEntity<?> eliminarImagen(@PathVariable Long idImagen) {
        boolean ok = productoService.eliminarImagenPorIdInmediato(idImagen);

        if (ok) {
            return ResponseEntity.ok(Map.of("success", true));
        } else {
            return ResponseEntity.status(400).body(Map.of("success", false, "message", "No se pudo eliminar"));
        }
    }
}

