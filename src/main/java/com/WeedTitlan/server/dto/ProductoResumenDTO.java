package com.WeedTitlan.server.dto;

public class ProductoResumenDTO {

    private Long id;
    private String productName;
    private double price;
    private Boolean tienePromocion;
    private Double porcentajeDescuento;
    private String imagenUrl;
    private String categoria;


    public ProductoResumenDTO(Long id, String productName, double price,
                              Boolean tienePromocion, Double porcentajeDescuento, String imagenUrl, String categoria) {
        this.id = id;
        this.productName = productName;
        this.price = price;
        this.tienePromocion = tienePromocion != null ? tienePromocion : false;
        this.porcentajeDescuento = porcentajeDescuento != null ? porcentajeDescuento : 0.0;
        this.imagenUrl = imagenUrl;
        this.categoria = categoria; 
    }

    public double getPrecioConDescuento() {
        if (tienePromocion && porcentajeDescuento > 0) {
            return price - (price * porcentajeDescuento / 100);
        }
        return price;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public double getPrice() {
        return price;
    }

    public Boolean getTienePromocion() {
        return tienePromocion;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}
}
