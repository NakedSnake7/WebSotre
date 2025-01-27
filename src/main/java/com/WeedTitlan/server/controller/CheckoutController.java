package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.CheckoutRequestDTO;       
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.UserService;

import jakarta.validation.Valid;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderItem;
import com.WeedTitlan.server.model.User;
import com.WeedTitlan.server.model.OrderStatus;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
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
    @CrossOrigin(origins = "http://localhost:8080")	
    public ResponseEntity<?> processCheckout(@Valid @RequestBody CheckoutRequestDTO checkoutRequest) {

        logger.info("Datos recibidos en el servidor: {}", checkoutRequest);

        try {
            // Buscar o crear usuario
            User user = userService.findOrCreateUserByEmail(
                checkoutRequest.getCustomer().getEmail(),
                checkoutRequest.getCustomer().getFullName()
            );
         // Obtener el nombre del cliente
            
            String customerName = checkoutRequest.getCustomer().getFullName();
            // Crear la orden
            Order order = new Order(
                user,
                checkoutRequest.getTotalAmount(),
                OrderStatus.PENDING,
                LocalDate.now(),
                checkoutRequest.getCustomer().getPhone(),
                checkoutRequest.getCustomer().getAddress(),
                customerName  // Asignar el nombre del cliente
            );

            // Crear los ítems de la orden
            checkoutRequest.getCart().forEach(cartItem -> {
            	OrderItem item = new OrderItem(
            	        cartItem.getName(),        // Nombre del producto
            	        cartItem.getQuantity(),    // Cantidad
            	        cartItem.getPrice(),       // Precio (asegúrate de que cartItem tenga el precio)
            	        order                      // La orden a la que pertenece
            	    );
            	    order.addItem(item);  // Añade el ítem a la orden
            	});
            // Guardar la orden
            orderService.saveOrder(order);

            logger.debug("Orden creada exitosamente para el usuario: {}", user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "Orden procesada exitosamente"
            ));
        } catch (Exception e) {
            logger.error("Error inesperado al procesar la orden: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "Error inesperado al procesar la orden"
            ));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "success", false,
            "errors", errors
        ));
    }
}
