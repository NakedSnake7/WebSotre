package com.WeedTitlan.server.dto;

import jakarta.validation.constraints.*;

public class ProductDTO {

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    private String productName;

    @NotNull(message = "La cantidad no puede ser nula")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer quantity;

    // Getters y setters
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}