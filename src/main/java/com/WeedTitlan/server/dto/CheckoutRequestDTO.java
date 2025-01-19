package com.WeedTitlan.server.dto;

import java.util.List;

public class CheckoutRequestDTO {

    private CustomerDTO customer; // Información del cliente
    private List<CartItemDTO> cart; // Productos en el carrito
    private Double totalAmount; // Monto total del pedido
    private String phone;   // Agrega el campo para el teléfono
    private String address; // Agrega el campo para la dirección
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
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
