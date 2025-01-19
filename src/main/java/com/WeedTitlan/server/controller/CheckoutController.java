// src/main/java/com/WeedTitlan/server/controller/CheckoutController.java

package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.CheckoutRequestDTO;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.User;
import com.WeedTitlan.server.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    @Autowired
    private OrderService orderService;

    // Endpoint para procesar checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@RequestBody CheckoutRequestDTO checkoutRequest) {
        try {
            // Crear el usuario (o buscarlo en la base de datos si ya existe)
            User user = new User(checkoutRequest.getCustomer().getFullName(), checkoutRequest.getCustomer().getEmail());

            // Crear la orden con los nuevos campos
            Order order = new Order(
                user, 
                checkoutRequest.getTotalAmount(), 
                OrderStatus.PENDING, 
                LocalDate.now(), 
                checkoutRequest.getPhone(), // Asegúrate de tener estos campos en CheckoutRequestDTO
                checkoutRequest.getAddress()
            );

            // Guardar la orden en la base de datos
            orderService.saveOrder(order);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Orden procesada exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al procesar la orden");
        }
    }
}
