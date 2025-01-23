package com.WeedTitlan.server.dto;

import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CheckoutRequestDTO {
    
	@NotNull(message = "El cliente no puede ser nulo")
    private CustomerDTO customer; // Información del cliente
    private List<CartItemDTO> cart; // Productos en el carrito
    @NotNull(message = "El monto total no puede ser nulo")
    @DecimalMin(value = "0.01", message = "El monto total debe ser mayor a 0")
    private Double totalAmount; // Monto total del pedido
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
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
