package com.WeedTitlan.server.exceptions;

public class UserNotFoundException extends RuntimeException {

    // Agregar serialVersionUID
    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }
}