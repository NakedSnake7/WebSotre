package com.WeedTitlan.server.dto;

import jakarta.validation.constraints.DecimalMin; 
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CartItemDTO {
    
    @NotNull(message = "El ID del producto no puede estar vacío")
    @NotBlank(message = "El nombre del producto no puede estar vacío")
    private String name; // El nombre del producto
    
    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad mínima debe ser 1")
    @DecimalMin(value = "1", message = "La cantidad debe ser al menos 1")
    private Integer quantity; // Cantidad de productos
    
    @NotNull(message = "El precio no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private Double price; // Precio del producto
  
    // Getters y Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }
}
