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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

    private static final double LIMITE_ENVIO_GRATIS = 1250.0;
    private static final double COSTO_ENVIO = 120.0; // O el que estés usando actualmente


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
            String direccion = checkoutRequest.getCustomer().getAddress();
            if (direccion == null || direccion.trim().length() < 5) {
                logger.error("Dirección inválida: {}", direccion);
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

            // Totales
            double subtotal = checkoutRequest.getCart().stream()
                    .mapToDouble(item -> item.getPrice() * item.getQuantity())
                    .sum();

            // Envío basado en subtotal real
            double costoEnvio = subtotal >= LIMITE_ENVIO_GRATIS ? 0.0 : COSTO_ENVIO;

            // Total final
            double totalFinal = subtotal + costoEnvio;

            logger.info("Subtotal: {}, Envío: {}, Total Final: {}", subtotal, costoEnvio, totalFinal);

            // Crear Orden
            Order order = new Order(
                    user,
                    totalFinal,
                    OrderStatus.CREATED,
                    direccion,
                    checkoutRequest.getCustomer().getFullName()
            );


            // Agregar productos
            checkoutRequest.getCart().forEach(cartItem -> {
                Producto producto = productoRepository.findByProductNameConTodo(cartItem.getName())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + cartItem.getName()));

                OrderItem item = new OrderItem(
                        producto,
                        cartItem.getQuantity(),
                        cartItem.getPrice(),
                        order
                );

                order.addItem(item);
            });

         // Guardar la orden
            orderService.saveOrder(order);

        

            // Cargar plantilla
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("email/email-template.html");
            if (inputStream == null) {
                logger.error("No se encontró la plantilla email/email-template.html");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error al cargar la plantilla de correo.");
            }

            String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            // Formato de moneda
            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
            symbols.setDecimalSeparator('.');
            symbols.setGroupingSeparator(',');
            DecimalFormat formatoMoneda = new DecimalFormat("#,##0.00", symbols);

            // Construir tabla de productos
            StringBuilder tablaProductos = new StringBuilder();
            double totalSinEnvio = 0;

            for (OrderItem item : order.getItems()) {
                double subtotalItem = item.getPrice() * item.getQuantity();
                totalSinEnvio += subtotalItem;

                tablaProductos.append("<tr>")
                .append("<td style='padding:10px; border-bottom:1px solid #2f2f2f;'>")
                    .append("<table cellpadding='0' cellspacing='0' border='0' style='width:100%;'>")
                        .append("<tr>")
                            .append("<td width='70' style='width:70px; padding-right:10px;'>")
                                .append("<img src='").append(item.getProducto().getImageUrl()).append("'")
                                .append(" width='70' height='70'")
                                .append(" style='display:block; width:70px !important; height:70px !important;")
                                .append(" max-width:70px !important; max-height:70px !important; object-fit:cover; border-radius:8px;'>")
                            .append("</td>")
                            .append("<td style='color:#fff; font-size:15px;'>")
                                .append(item.getProducto().getProductName())
                            .append("</td>")
                        .append("</tr>")
                    .append("</table>")
                .append("</td>")
                .append("<td style='padding:10px; color:#fff; text-align:center; border-bottom:1px solid #2f2f2f;'>")
                    .append(item.getQuantity())
                .append("</td>")
                .append("<td style='padding:10px; color:#fff; text-align:center; border-bottom:1px solid #2f2f2f;'>")
                    .append("$").append(formatoMoneda.format(subtotalItem))
                .append("</td>")
            .append("</tr>");

            }

            // Subtotal
            tablaProductos.append("<tr>")
                    .append("<td colspan='2' style='font-weight:bold;'>Subtotal</td>")
                    .append("<td>$").append(formatoMoneda.format(totalSinEnvio)).append("</td>")
                    .append("</tr>");

            // Envío
            tablaProductos.append("<tr>")
            .append("<td colspan='2' style='font-weight:bold;'>Envío</td>")
            .append("<td>")
            .append(Double.compare(costoEnvio, 0.0) == 0 
                    ? "GRATIS" 
                    : "$" + formatoMoneda.format(costoEnvio))
            .append("</td></tr>");


            // Total Final
            tablaProductos.append("<tr style='background-color:#2e7d32; color:white; font-weight:bold;'>")
                    .append("<td colspan='2'>Total Final</td>")
                    .append("<td>$").append(formatoMoneda.format(totalFinal)).append("</td>")
                    .append("</tr>");

            // Reemplazar datos
            String emailHTML = template
                    .replace("{NOMBRE}", user.getFullName())
                    .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
                    .replace("{LISTADO_PRODUCTOS}", tablaProductos.toString())
                    .replace("{TOTAL}", "$" + formatoMoneda.format(totalFinal));

            // Enviar correo
            try {
                logger.info("Enviando correo a {}", user.getEmail());
                emailService.enviarCorreoHTML(user.getEmail(), "Confirmación de Compra", emailHTML);
                order.setEmailSent(true);
            } catch (Exception e) {
                logger.error("Error enviando correo: ", e.getMessage());
                order.setEmailSent(false);
            }
            orderService.saveOrder(order);
          
               
            
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "success", true,
                            "orderId", order.getId(),
                            "message", "¡Orden exitosa!, revisa tu correo electrónico."
                    ));
  
            
            
        } catch (Exception e) {
            logger.error("Error inesperado: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error inesperado al procesar la orden."));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "errors", errors));
    }
}
