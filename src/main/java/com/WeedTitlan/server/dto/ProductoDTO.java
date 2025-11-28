package com.WeedTitlan.server.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;

public class ProductoDTO {

    private Long id;

    // Nombre obligatorio
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres")
    private String productName;

    // Precio obligatorio
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private Double price;

    // Descripción similar a la entidad
    @Lob
    private String description;

    // Categoría seleccionada existente
    @Size(max = 50, message = "El nombre de categoría es demasiado largo")
    private String categoriaNombre;

    // Nueva categoría que se crea
    @Size(max = 50, message = "El nombre de la nueva categoría es demasiado largo")
    private String nuevaCategoria;

    // Stock
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    // Descuento
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double porcentajeDescuento;


    private Boolean visibleEnMenu;
    private Boolean tienePromocion;

    // IDs de imágenes existentes (para eliminar o mostrar en formulario)
    private List<Long> imagenesExistentes = new ArrayList<>();

    // URLs de las imágenes existentes (opcional para mostrar en Thymeleaf)
    private List<String> urlsImagenesExistentes = new ArrayList<>();
    // ---- Getters y setters ---- //

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

    public Double getPrice() {
        return price;
    }
    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }
    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getNuevaCategoria() {
        return nuevaCategoria;
    }
    public void setNuevaCategoria(String nuevaCategoria) {
        this.nuevaCategoria = nuevaCategoria;
    }

    public Integer getStock() {
        return stock;
    }
    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Double getPorcentajeDescuento() {
        return porcentajeDescuento;
    }

    public void setPorcentajeDescuento(Double porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento;
    }
    public void setPorcentajeDescuento(Integer porcentajeDescuento) {
        this.porcentajeDescuento = porcentajeDescuento != null ? porcentajeDescuento.doubleValue() : null;
    }



    public Boolean getVisibleEnMenu() {
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
	public List<Long> getImagenesExistentes() {
		return imagenesExistentes;
	}
	public void setImagenesExistentes(List<Long> imagenesExistentes) {
		this.imagenesExistentes = imagenesExistentes;
	}
	public List<String> getUrlsImagenesExistentes() {
		return urlsImagenesExistentes;
	}
	public void setUrlsImagenesExistentes(List<String> urlsImagenesExistentes) {
		this.urlsImagenesExistentes = urlsImagenesExistentes;
	}
}
