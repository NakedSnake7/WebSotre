package com.WeedTitlan.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.WeedTitlan.server.service.ProductoService;

@RestController
@RequestMapping("/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PutMapping("/{id}/stock/{cantidad}")
    public ResponseEntity<String> actualizarStock(@PathVariable Long id, @PathVariable int cantidad) {
        try {
            productoService.actualizarStock(id, cantidad);
            return ResponseEntity.ok("Stock actualizado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

