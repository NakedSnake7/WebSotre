package com.WeedTitlan.server.model;

import jakarta.persistence.*;

@Entity
@Table(name = "imagenes_productos")
public class ImagenProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false) // Guarda la URL de la imagen
    private String imageUrl;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;
 // Constructor que acepta la URL y el producto
    public ImagenProducto(String imageUrl, Producto producto) {
        this.imageUrl = imageUrl;
        this.producto = producto;
    }
    public ImagenProducto() {
        // Constructor vacío necesario para JPA y otras instancias manuales
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }
}
