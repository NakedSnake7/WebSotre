package com.WeedTitlan.server;

import jakarta.validation.Valid; 
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class Controller {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OrderService orderService;

    public Controller(UserService userService, PasswordEncoder passwordEncoder, OrderService orderService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.orderService = orderService;
    }
    
    // Método para procesar el checkout
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckout(@Valid @RequestBody Order order, BindingResult result) {
        // Validar datos de la orden
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorMessage(result));
        }

        // Guardar la orden
        try {
            Order savedOrder = orderService.saveOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseMessage("Orden procesada exitosamente", savedOrder)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al procesar la orden: " + e.getMessage());
        }
    }

    // Método para registrar un usuario
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody User user, BindingResult result) {
        // Validación de errores
        if (result.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorMessage(result));
        }

        // Verificar si el correo electrónico ya está registrado
        if (userService.existsByEmail(user.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("El correo electrónico ya está registrado.");
        }

        // Cifrar la contraseña antes de guardarla
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        // Guardar el usuario
        try {
            User savedUser = userService.saveUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseMessage("Registro exitoso", savedUser)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al registrar el usuario: " + e.getMessage());
        }
    }

    // Método para construir el mensaje de error
    private String buildErrorMessage(BindingResult result) {
        StringBuilder errorMessage = new StringBuilder();
        result.getAllErrors().forEach(error -> errorMessage.append(error.getDefaultMessage()).append(" "));
        return errorMessage.toString();
    }

    // Clase interna ResponseMessage
    public static class ResponseMessage {
        private String message;
        private Object data;  // Puede ser User u Order u otros tipos de datos

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
