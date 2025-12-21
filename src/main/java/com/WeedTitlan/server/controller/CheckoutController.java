package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.CheckoutRequestDTO;
import com.WeedTitlan.server.model.*;
import com.WeedTitlan.server.model.Order.PaymentMethod;
import com.WeedTitlan.server.repository.ProductoRepository;
import com.WeedTitlan.server.service.EmailService;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.UserService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private static final double LIMITE_ENVIO_GRATIS = 1250.0;
    private static final double COSTO_ENVIO = 120.0;

    private final EmailService emailService;
    private final OrderService orderService;
    private final UserService userService;
    private final ProductoRepository productoRepository;

    public CheckoutController(
            EmailService emailService,
            OrderService orderService,
            UserService userService,
            ProductoRepository productoRepository
    ) {
        this.emailService = emailService;
        this.orderService = orderService;
        this.userService = userService;
        this.productoRepository = productoRepository;
    }

    @PostMapping("/checkout")
    @CrossOrigin(origins = {"http://localhost:8080", "https://weedtitlan.com"})
    public ResponseEntity<?> processCheckout(
            @Valid @RequestBody CheckoutRequestDTO checkoutRequest
    ) {

        try {
            // ============================
            // 1. VALIDAR STOCK (LOCK)
            // ============================
            orderService.validarStockCheckout(checkoutRequest);

            // ============================
            // 2. VALIDAR DIRECCIÓN
            // ============================
            String direccion = checkoutRequest.getCustomer().getAddress();
            if (direccion == null || direccion.trim().length() < 5) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "La dirección debe tener al menos 5 caracteres"
                        ));
            }

            // ============================
            // 3. USUARIO
            // ============================
            User user = userService.findOrCreateUserByEmail(
                    checkoutRequest.getCustomer().getEmail(),
                    checkoutRequest.getCustomer().getFullName(),
                    checkoutRequest.getCustomer().getPhone()
            );
            userService.saveUser(user);

            // ============================
            // 4. TOTALES
            // ============================
            double subtotal = checkoutRequest.getCart().stream()
                    .mapToDouble(i -> i.getPrice() * i.getQuantity())
                    .sum();

            double costoEnvio = subtotal >= LIMITE_ENVIO_GRATIS ? 0.0 : COSTO_ENVIO;
            double totalFinal = subtotal + costoEnvio;

            // ============================
            // 5. CREAR ORDEN
            // ============================
            Order order = new Order(
                    user,
                    totalFinal,
                    OrderStatus.CREATED,
                    direccion,
                    checkoutRequest.getCustomer().getFullName()
            );

            // 🔥 IMPORTANTE: definir método y estado de pago
            order.setPaymentMethod(PaymentMethod.TRANSFER); // o STRIPE
            order.setPaymentStatus(PaymentStatus.PENDING);

            // ============================
            // 6. ITEMS
            // ============================
            checkoutRequest.getCart().forEach(cartItem -> {
                Producto producto = productoRepository
                        .findByProductNameConTodo(cartItem.getName())
                        .orElseThrow(() ->
                                new RuntimeException("Producto no encontrado: " + cartItem.getName()));

                OrderItem item = new OrderItem(
                        producto,
                        cartItem.getQuantity(),
                        cartItem.getPrice(),
                        order
                );
                order.addItem(item);
            });

            // ============================
            // 7. GUARDAR ORDEN
            // 👉 El Service maneja:
            // - Descuento de stock (si aplica)
            // - Flags
            // - Expiración automática
            // ============================
            orderService.saveOrder(order);

            // ============================
            // 8. CORREO (NO CRÍTICO)
            // ============================
            try {
                InputStream inputStream = getClass().getClassLoader()
                        .getResourceAsStream("email/email-template.html");

                if (inputStream != null) {
                    String template = new String(
                            inputStream.readAllBytes(),
                            StandardCharsets.UTF_8
                    );

                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                    DecimalFormat formato = new DecimalFormat("#,##0.00", symbols);

                    StringBuilder tablaProductos = new StringBuilder();

                    for (OrderItem item : order.getItems()) {
                        double sub = item.getPrice() * item.getQuantity();

                        tablaProductos.append("<tr>")
                                .append("<td>").append(item.getProducto().getProductName()).append("</td>")
                                .append("<td>").append(item.getQuantity()).append("</td>")
                                .append("<td>$").append(formato.format(sub)).append("</td>")
                                .append("</tr>");
                    }

                    String emailHTML = template
                            .replace("{NOMBRE}", user.getFullName())
                            .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
                            .replace("{LISTADO_PRODUCTOS}", tablaProductos.toString())
                            .replace("{TOTAL}", "$" + formato.format(totalFinal));

                    emailService.enviarCorreoHTML(
                            user.getEmail(),
                            "Confirmación de Compra",
                            emailHTML
                    );

                    order.setEmailSent(true);
                    orderService.save(order); // solo flag
                }

            } catch (Exception e) {
                logger.error("Error enviando correo", e);
            }

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "orderId", order.getId(),
                            "message", "¡Orden creada correctamente!"
                    ));

        } catch (Exception e) {
            logger.error("Error en checkout", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error al procesar la orden"
                    ));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return ResponseEntity.badRequest()
                .body(Map.of(
                        "success", false,
                        "errors", errors
                ));
    }
}
