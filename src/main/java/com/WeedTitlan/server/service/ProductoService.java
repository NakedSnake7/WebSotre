package com.WeedTitlan.server.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.repository.ProductoRepository;

@Service
public class ProductoService {
    
    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> listarProductos() {
        return productoRepository.findAll();
    }

    public Producto obtenerProducto(Long id) {
        return productoRepository.findById(id).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    public void actualizarStock(Long id, int cantidad) {
        Producto producto = obtenerProducto(id);
        if (producto.getStock() >= cantidad) {
            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        } else {
            throw new RuntimeException("Stock insuficiente");
        }
    }
}
