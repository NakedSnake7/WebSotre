package com.WeedTitlan.server.model;

import jakarta.persistence.CascadeType;  
import jakarta.persistence.Column;
import jakarta.persistence.Entity;   
import jakarta.validation.constraints.Size;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.MERGE}, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "El usuario es obligatorio")
    private User user;

    private String customerName; 

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @NotNull(message = "El total de la orden no puede ser nulo")
    @Min(value = 0, message = "El total debe ser positivo")
    private Double total;

    @NotBlank(message = "La dirección no puede estar vacía")
    @Size(min = 5, max = 255, message = "La dirección debe tener entre 5 y 255 caracteres")
    private String address;

   

    @NotNull(message = "La fecha de la orden no puede ser nula")
    private LocalDateTime orderDate;

    

    
    
    @NotNull
    private Boolean emailSent = false; // Por defecto falso
    
    @Column(name = "expiration_email_sent", nullable = false)
    private boolean expirationEmailSent = false;

    @Column(name = "tracking_number")
    private String trackingNumber;

    @Column(name = "carrier")
    private String carrier;

    @Column(name = "stock_reduced")
    private Boolean stockReduced = false;
    
    
    @Column(name = "stripe_session_id")
    private String stripeSessionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false)
    private OrderStatus orderStatus = OrderStatus.CREATED;


    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;

    public enum PaymentMethod {
        CARD,
        TRANSFER
    }

    
    public Boolean getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(Boolean emailSent) {
        this.emailSent = emailSent;
    }

    
    // Constructor vacío necesario para JPA
    public Order() {}

    // Constructor sin 'phone'
    public Order(User user, Double total, OrderStatus status, String address, String customerName) {
        this.user = user;
        this.total = total;
        this.paymentStatus = PaymentStatus.PENDING;
        this.orderDate = LocalDateTime.now();
        this.address = address;
        this.customerName = customerName;
    }


    // Métodos para manejar la relación bidireccional
    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
        item.setOrder(null);
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

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }


    public @NotNull(message = "La fecha de la orden no puede ser nula") LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(@NotNull(message = "La fecha de la orden no puede ser nula") LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // Método toString para depuración
    @Override
    public String toString() {
        return "Order{id=" + id +
               ", user=" + (user != null ? user.getFullName() : "null") +
               ", total=" + total +
               ", orderStatus=" + orderStatus +
               ", paymentStatus=" + paymentStatus +
               ", orderDate=" + orderDate + "}";
    }


    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
    public String getCarrier() {
        return carrier;
    }
    // Getter
    public Boolean isStockReduced() {
        return stockReduced != null && stockReduced; // evita null
    }

    // Setter
    public void setStockReduced(Boolean stockReduced) {
        this.stockReduced = stockReduced;
    }

public boolean isExpirationEmailSent() {
    return expirationEmailSent;
}

public void setExpirationEmailSent(boolean expirationEmailSent) {
    this.expirationEmailSent = expirationEmailSent;
}
public String getStripeSessionId() {
    return stripeSessionId;
}

public void setStripeSessionId(String stripeSessionId) {
    this.stripeSessionId = stripeSessionId;
}
public PaymentMethod getPaymentMethod() {
    return paymentMethod;
}

public void setPaymentMethod(PaymentMethod paymentMethod) {
    this.paymentMethod = paymentMethod;
}

public PaymentStatus getPaymentStatus() {
    return paymentStatus;
}

public void setPaymentStatus(PaymentStatus paymentStatus) {
    this.paymentStatus = paymentStatus;
}

public OrderStatus getOrderStatus() {
    return orderStatus;
}

public void setOrderStatus(OrderStatus orderStatus) {
    this.orderStatus = orderStatus;
}


}
