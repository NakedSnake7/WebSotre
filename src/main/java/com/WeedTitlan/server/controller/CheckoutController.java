// src/main/java/com/WeedTitlan/server/controller/CheckoutController.java

package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.CheckoutRequestDTO; 
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.UserService;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.User;
import com.WeedTitlan.server.model.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private UserService userService; // Inyectar el UserService
    
    // Endpoint para procesar checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@RequestBody CheckoutRequestDTO checkoutRequest) {
        try {
            // Validar datos de entrada
            if (checkoutRequest.getCustomer() == null || 
                checkoutRequest.getCustomer().getEmail() == null || 
                checkoutRequest.getCustomer().getFullName() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Información del cliente incompleta");
            }

            // Buscar o crear al usuario
            User user = userService.findUserByEmail(checkoutRequest.getCustomer().getEmail());
            if (user == null) {
            	
                user = new User(
                    checkoutRequest.getCustomer().getFullName(), 
                    checkoutRequest.getCustomer().getEmail()
                );
                user = userService.saveUser(user);
                throw new IllegalStateException("El usuario no se guardó correctamente");
            }
          
            // Crear y guardar la orden
            Order order = new Order(
                user, 
                checkoutRequest.getTotalAmount(), 
                OrderStatus.PENDING, 
                LocalDate.now(), 
                checkoutRequest.getPhone(), 
                checkoutRequest.getAddress()
            );
            orderService.saveOrder(order);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Orden procesada exitosamente");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El correo electrónico ya está registrado");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Datos inválidos: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log del error para depuración
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error inesperado al procesar la orden");
        }
    }
}
