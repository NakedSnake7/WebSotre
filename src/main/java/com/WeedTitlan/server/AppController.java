package com.WeedTitlan.server;

import jakarta.validation.Valid; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
	
@Controller 
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AppController {

    private final UserService userService;
    private final OrderService orderService;

    public AppController(UserService userService, OrderService orderService) {
        this.userService = userService;
        this.orderService = orderService;
    }

    // Endpoint para suscribir usuarios
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody SubscriptionRequest request, BindingResult result) {
        // Validar errores en la solicitud
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildValidationErrorResponse(result));
        }
        
        

        // Verificar si el correo ya está registrado
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT) // Cambiado a 409 CONFLICT
                    .body(new ResponseMessage("El correo electrónico ya está registrado.", null));
        }

        try {
            // Crear y guardar el usuario
            User newUser = new User(request.getName(), request.getEmail());
            userService.saveUser(newUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseMessage("Suscripción exitosa", newUser));
        } catch (Exception e) {
            // Registrar la excepción para diagnóstico
            System.err.println("Error al guardar el usuario: " + e.getMessage()); // Cambiar a un logger en producción
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage("Ocurrió un error interno. Intente nuevamente más tarde.", null));
        }
    }
    
    @GetMapping("/checkout")
    public String showCheckoutPage() {
        return "checkout"; // Nombre del archivo en `src/main/resources/templates/checkout.html`
    }
    

    // Método para construir mensajes de error de validación
    private ResponseMessage buildValidationErrorResponse(BindingResult result) {
        StringBuilder errorMessage = new StringBuilder();
        result.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append(". "));
        return new ResponseMessage("Error en la validación", errorMessage.toString().trim());
    }
   
    // Endpoint para procesar checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@Valid @RequestBody Order order, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(buildValidationErrorResponse(result));
        }

        try {
            Order savedOrder = orderService.saveOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseMessage("Orden procesada exitosamente", savedOrder));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage("Error al procesar la orden", e.getMessage()));
        }
    }

    // Clase interna para manejar solicitudes de suscripción
    public static class SubscriptionRequest {
        private String name;
        private String email;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    // Clase para las respuestas personalizadas
    public static class ResponseMessage {
        private String message;
        private Object data;

        public ResponseMessage(String message, Object data) {
            this.message = message;
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
}
