package com.WeedTitlan.server.model;

import jakarta.persistence.CascadeType;   
import jakarta.persistence.Entity;   
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;


@Entity
@Table(name = "orders") // Cambiar nombre de la tabla a "orders"
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "user_id", nullable = false) // Define la columna de la clave foránea
    @NotNull(message = "El usuario es obligatorio")
    private User user;
    
    @NotBlank(message = "Los nombres de los productos no pueden estar vacíos")
    private String productNames; // Lista de nombres de productos en formato texto

    public String getProductNames() {
        return productNames;
    }

    public void setProductNames(String productNames) {
        this.productNames = productNames;
    }

    
    @NotNull(message = "El total de la orden no puede ser nulo")
    private Double total;
    
    @NotBlank(message = "El telefono no puede estar vacía")
    @NotNull(message = "El teléfono no puede estar vacío")
    @Pattern(regexp = "^[0-9]{10}$", message = "El teléfono debe tener 10 dígitos")
    @Size(min = 10, max = 10, message = "El teléfono debe tener 10 dígitos")
    private String phone;

    @NotNull(message = "La dirección no puede estar vacía")
    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String address;

    @NotNull(message = "El estado de la orden no puede estar vacío")
    @Enumerated(EnumType.STRING)  // Asegúrate de que el Enum se guarde como un String en la base de datos
    private OrderStatus status;  // Usando Enum para el estado

    @NotNull(message = "La fecha de la orden no puede ser nula")
    private LocalDate orderDate;

    // Constructor vacío necesario para JPA
    public Order() {}

    // Constructor con parámetros
    public Order(User user, Double total, OrderStatus status, LocalDate orderDate, String phone, String address) {
        this.user = user;
        this.total = total;
        this.status = status;
        this.orderDate = orderDate;
        this.phone = phone;
        this.address = address;
    }

    // Getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
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

    // Método toString para facilitar la depuración
    @Override
    public String toString() {
        return "Order{id=" + id + ", user=" + user.getName() + 
               ", total=" + total + ", status=" + status + 
               ", orderDate=" + orderDate + ", phone=" + phone + 
               ", address=" + address + "}";
    }
}
