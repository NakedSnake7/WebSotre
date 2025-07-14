package com.WeedTitlan.server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Coupon {

    @Id
    private String code;

    private double discountPercentage;

    private boolean active;

    // Getters y Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
