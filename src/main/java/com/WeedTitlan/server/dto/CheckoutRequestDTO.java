package com.WeedTitlan.server.dto;

import java.util.List;  

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CheckoutRequestDTO {
    
    @NotNull(message = "El cliente no puede ser nulo")
    @Valid
    private CustomerDTO customer; // Información del cliente
    @NotNull(message = "El carrito no puede ser nulo")
    @NotEmpty(message = "El carrito no puede estar vacío")
    @Valid
    private List<CartItemDTO> cart; // Productos en el carrito
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    @NotNull(message = "El monto total no puede ser nulo")
    private Double totalAmount; // Monto total del pedido
    // ✅ Nuevo campo: código de cupón
    private String couponCode;
    // Getters y Setters
    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    public List<CartItemDTO> getCart() {
        return cart;
    }

    public void setCart(List<CartItemDTO> cart) {
        this.cart = cart;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    // Método para derivar los nombres de productos
    public List<String> getProductNames() {
        return cart.stream()
                   .map(CartItemDTO::getName) // Suponiendo que CartItemDTO tiene un campo productName
                   .toList();
    }
}