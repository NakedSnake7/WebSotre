package com.WeedTitlan.server;

import java.util.List;  
import com.WeedTitlan.server.dto.CartItemDTO;
import com.WeedTitlan.server.dto.CustomerDTO;


public class CheckoutRequest {
    private CustomerDTO customer;
    private List<CartItemDTO> cart;
    private Double totalAmount;

    
    // Getters y setters

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
    
}

