package com.WeedTitlan.server.model;

public enum PaymentStatus {
    PENDING,     // esperando pago (Stripe / transferencia)
    PAID,        // pago confirmado
    FAILED,      // pago fallido
    EXPIRED      // pago nunca llegó
}
