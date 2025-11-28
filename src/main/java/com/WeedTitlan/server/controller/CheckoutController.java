package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.CheckoutRequestDTO;
import com.WeedTitlan.server.model.*;
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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private static final double LIMITE_ENVIO_GRATIS = 800.0;
    private static final double COSTO_ENVIO = 100.0;

    private final EmailService emailService;
    private final OrderService orderService;
    private final UserService userService;
    private final ProductoRepository productoRepository;

    public CheckoutController(EmailService emailService, OrderService orderService,
                              UserService userService, ProductoRepository productoRepository) {
        this.emailService = emailService;
        this.orderService = orderService;
        this.userService = userService;
        this.productoRepository = productoRepository;
    }

    @PostMapping("/checkout")
    @CrossOrigin(origins = {"http://localhost:8080", "https://weedtitlan.com"})
    public ResponseEntity<?> processCheckout(@Valid @RequestBody CheckoutRequestDTO checkoutRequest) {
        try {
            // Validación mínima de dirección
            if (checkoutRequest.getCustomer().getAddress() == null ||
                checkoutRequest.getCustomer().getAddress().length() < 5) {
                logger.error("Error: La dirección debe tener entre 5 y 255 caracteres.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Error: La dirección debe tener al menos 5 caracteres.");
            }

            // Buscar o crear usuario
            User user = userService.findOrCreateUserByEmail(
                    checkoutRequest.getCustomer().getEmail(),
                    checkoutRequest.getCustomer().getFullName(),
                    checkoutRequest.getCustomer().getPhone()
            );
            userService.saveUser(user);

            // Calcular total con envío
            double subtotal = checkoutRequest.getTotalAmount();
            double costoEnvio = subtotal >= LIMITE_ENVIO_GRATIS ? 0.0 : COSTO_ENVIO;
            double totalFinal = subtotal + costoEnvio;

            // Crear la orden
            Order order = new Order(user, totalFinal, OrderStatus.PENDING,
                    LocalDate.now(), checkoutRequest.getCustomer().getAddress(),
                    checkoutRequest.getCustomer().getFullName());

            // Agregar los items
            checkoutRequest.getCart().forEach(cartItem -> {
                Producto producto = productoRepository.findByProductNameConTodo(cartItem.getName())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + cartItem.getName()));
                OrderItem item = new OrderItem(producto, cartItem.getQuantity(), cartItem.getPrice(), order);
                order.addItem(item);
            });

            // Guardar la orden
            orderService.saveOrder(order);

            // Preparar plantilla de correo
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("templates/email-template.html");
            if (inputStream == null) {
                logger.error("Error: No se pudo cargar la plantilla de email.");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error: No se pudo cargar la plantilla de email.");
            }

            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Construir tabla HTML de productos
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat formatoMoneda = new DecimalFormat("#,##0.00", symbols);

            StringBuilder tablaProductos = new StringBuilder();
            double totalGeneral = 0;
            for (OrderItem item : order.getItems()) {
                double subtotalItem = item.getPrice() * item.getQuantity();
                totalGeneral += subtotalItem;

                tablaProductos.append("<tr>")
                        .append("<td>")
                            .append("<div class='product-row'>")
                                .append("<img src='").append(item.getProducto().getImageUrl()).append("' class='product-img'>")
                                .append("<span>").append(item.getProducto().getProductName()).append("</span>")
                            .append("</div>")
                        .append("</td>")
                        .append("<td>").append(item.getQuantity()).append("</td>")
                        .append("<td>$").append(formatoMoneda.format(subtotalItem)).append("</td>")
                        .append("</tr>");
            }


            tablaProductos.append("<tr style='background-color:#2e7d32; color:#fff; font-weight:bold;'>")
                    .append("<td colspan='2' style='padding:8px; border:1px solid #444;'>Total</td>")
                    .append("<td style='padding:8px; border:1px solid #444;'>$").append(formatoMoneda.format(totalGeneral)).append("</td>")
                    .append("</tr>");

            // Reemplazar datos en plantilla
            String emailHTMLConProductos = template
                    .replace("{NOMBRE}", user.getFullName())
                    .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
                    .replace("{TOTAL}", "$" + order.getTotal())
                    .replace("{LISTADO_PRODUCTOS}", tablaProductos.toString());

            // Intentar enviar correo sin romper la compra
            try {
                logger.info("➡️ Enviando correo a {}", user.getEmail());
                emailService.enviarCorreoHTML(user.getEmail(), "Confirmación de Compra", emailHTMLConProductos);
                order.setEmailSent(true);
                orderService.saveOrder(order); // Actualizar emailSent
            } catch (Exception e) {
                logger.error("Error al enviar el correo: ", e);
                order.setEmailSent(false);
                orderService.saveOrder(order); // Guardar estado aunque falle
            }

            logger.debug("Orden creada exitosamente para el usuario: {}", user.getEmail());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "message", "¡Orden exitosa!, favor de revisar su correo electrónico."));

        } catch (Exception e) {
            logger.error("Error inesperado al procesar la orden: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error inesperado al procesar la orden"));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "errors", errors));
    }
}
