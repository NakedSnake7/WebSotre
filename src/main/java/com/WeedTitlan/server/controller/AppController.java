package com.WeedTitlan.server.controller;

import jakarta.validation.Valid;   


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import com.WeedTitlan.server.model.User;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.UserService;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Controller 
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AppController {

    private final UserService userService;

    public AppController(UserService userService, OrderService orderService) {
        this.userService = userService;
      
    }

    // Endpoint para suscribir usuarios
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@Valid @RequestBody SubscriptionRequest request, BindingResult result) {
    	  // Verificar si los datos de la solicitud son correctos
        System.out.println("Datos recibidos: " + request.getFullName() + ", " + request.getEmail());
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
        	User newUser = new User(request.getFullName(), request.getEmail(), null);
        	System.out.println("Datos recibidos: Nombre = " + request.getFullName() + ", Email = " + request.getEmail());
            userService.saveUser(newUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ResponseMessage("Suscripción exitosa", newUser));
        } catch (Exception e) {
            // Registrar la excepción para diagnóstico
        	
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseMessage("Ocurrió un error interno. Intente nuevamente más tarde.", null));
        }
    }
 
 

    // Método para construir mensajes de error de validación
    private ResponseMessage buildValidationErrorResponse(BindingResult result) {
        StringBuilder errorMessage = new StringBuilder();
        result.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append(". "));
        return new ResponseMessage("Error en la validación", errorMessage.toString().trim());
    }

    // Clase interna para manejar solicitudes de suscripción
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscriptionRequest {
        private String fullName;
        private String email;
       

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
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
