package com.WeedTitlan.server.model;

public enum OrderStatus {
    PENDING,   // Cambiado de PENDIENTE a PENDING
    PROCESSED, // Cambiado de PAGADO a PROCESSED
    DELIVERED, // Cambiado de CONFIRMADO a DELIVERED
    SHIPPED    // Cambiado de CANCELADO a SHIPPED
}
