package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.CheckoutRequestDTO; 
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.UserService;

import jakarta.validation.Valid;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.User;
import com.WeedTitlan.server.model.OrderStatus;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@Valid @RequestBody CheckoutRequestDTO checkoutRequest) {
        try {
            // Buscar o crear al usuario
            User user = userService.findOrCreateUserByEmail(
                checkoutRequest.getCustomer().getEmail(),
                checkoutRequest.getCustomer().getFullName()
            );

            // Crear la orden
            Order order = new Order(
                user,
                checkoutRequest.getTotalAmount(),
                OrderStatus.PENDING,
                LocalDate.now(),
                checkoutRequest.getPhone(),
                checkoutRequest.getAddress()
            );

            // Guardar la orden
            orderService.saveOrder(order);

            // Respuesta de éxito
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Orden procesada exitosamente");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (DataIntegrityViolationException e) {
            logger.error("Conflicto al procesar la orden: ", e);
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Collections.singletonMap("message", "El correo electrónico ya está registrado"));
        } catch (Exception e) {
            logger.error("Error inesperado al procesar la orden: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", "Error inesperado al procesar la orden"));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
