package com.WeedTitlan.server.exceptions;

public class EmailAlreadyExistsException extends RuntimeException {
    
    // Agregar serialVersionUID
    private static final long serialVersionUID = 1L;

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}