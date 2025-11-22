package com.WeedTitlan.server.model;

public class ProductoProveedor {
    private String nombre;
    private int stock;

    public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public ProductoProveedor(String nombre, int stock) {
        this.nombre = nombre;
        this.stock = stock;
    }

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

    // Getters y setters
}
