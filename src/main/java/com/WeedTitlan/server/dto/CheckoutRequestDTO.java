package com.WeedTitlan.server.dto;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class CheckoutRequestDTO {

    @NotNull(message = "El cliente no puede ser nulo")
    @Valid
    private CustomerDTO customer;

    @NotNull(message = "El carrito no puede ser nulo")
    @NotEmpty(message = "El carrito no puede estar vacío")
    @Valid
    private List<CartItemDTO> cart;

    // Opcional
    private String couponCode;

    @NotNull(message = "El método de pago es obligatorio")
    private String paymentMethod;

    // ===== Getters y Setters =====

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

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<String> getProductNames() {
        return cart.stream()
                   .map(CartItemDTO::getName)
                   .toList();
    }
}
