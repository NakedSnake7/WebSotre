package com.WeedTitlan.server.model;

import java.util.ArrayList;    
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;  
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;



@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name")
    private String productName;

    private double price;
    private int stock;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id") // clave foránea
    private Categoria categoria;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ImagenProducto> imagenes = new ArrayList<>();

    @Column(nullable = false)
    private boolean visibleEnMenu = true;
    
    @Column(name = "tiene_promocion", nullable = false)
    private Boolean tienePromocion;

    @Column(name = "porcentaje_descuento", nullable = false)
    private Double porcentajeDescuento;

    
    public double getPrecioConDescuento() {
        if (tienePromocion && porcentajeDescuento != null) {
            return price - (price * porcentajeDescuento / 100);
        }
        return price;
    } 

    
    //vacio
    public Producto() {
        this.visibleEnMenu = true;
        this.tienePromocion = false;
        this.porcentajeDescuento = 0.0;
    }

 // Constructor completo
    public Producto(String productName, double price, int stock, String description, Categoria categoria) {
        this.productName = productName;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.categoria = categoria;
    }
    public void actualizarDatosDesde(Producto producto, Categoria categoria) {
        this.productName = producto.getProductName();
        this.price = producto.getPrice();
        this.stock = producto.getStock();
        this.description = producto.getDescription();
        this.categoria = categoria;
    }



    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ImagenProducto> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenProducto> imagenes) {
        this.imagenes = imagenes;
    }

    public boolean isVisibleEnMenu() {
        return visibleEnMenu;
    }

    public void setVisibleEnMenu(Boolean visibleEnMenu) {
        this.visibleEnMenu = visibleEnMenu;
    }
    public Boolean getTienePromocion() {
        return tienePromocion;
    }

    public void setTienePromocion(Boolean tienePromocion) {
        this.tienePromocion = tienePromocion;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }

}
