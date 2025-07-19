package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.dto.CheckoutRequestDTO;  
import com.WeedTitlan.server.service.EmailService;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.UserService;


import jakarta.validation.Valid;


import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderItem;
import com.WeedTitlan.server.model.User;
import com.WeedTitlan.server.repository.ProductoRepository;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.Producto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

//import java.nio.file.Files;
//import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;


@RestController
@RequestMapping("/api")
public class CheckoutController {

	@Autowired
	private EmailService emailService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@Autowired
	private ProductoRepository productoRepository;
	
	
	//@Autowired
	//private WhatsappService whatsappService;

	private static final Logger logger = LoggerFactory.getLogger(CheckoutController.class);

	private static final double LIMITE_ENVIO_GRATIS = 800.0; // ejemplo
	private static final double COSTO_ENVIO = 100.0;

	
	@PostMapping("/checkout")
	@CrossOrigin(origins = {"http://localhost:8080", "https://weedtitlan.com"})
	public ResponseEntity<?> processCheckout(@Valid @RequestBody CheckoutRequestDTO checkoutRequest ) {
		  
		

		try {
			if (checkoutRequest.getCustomer().getAddress() == null
					|| checkoutRequest.getCustomer().getAddress().length() < 5) {
				logger.error("Error: La dirección debe tener entre 5 y 255 caracteres.");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body("Error: La dirección debe tener al menos 5 caracteres.");
			}

			// Validar antes de procesar la orden
		
              
			User user = userService.findOrCreateUserByEmail(
				    checkoutRequest.getCustomer().getEmail(),
				    checkoutRequest.getCustomer().getFullName(),
				    checkoutRequest.getCustomer().getPhone()
				);
			
			userService.saveUser(user);  // Asegúrate de que el método `saveUser` esté guardando los datos correctamente


			// String customerName = checkoutRequest.getCustomer().getFullName();
			double subtotal = checkoutRequest.getTotalAmount();
			double costoEnvio = subtotal >= LIMITE_ENVIO_GRATIS ? 0.0 : COSTO_ENVIO;
			double totalFinal = subtotal + costoEnvio;

			// Crear la orden
			Order order = new Order(user, totalFinal, OrderStatus.PENDING, LocalDate.now(),
					checkoutRequest.getCustomer().getAddress(), checkoutRequest.getCustomer().getFullName());

			// Crear los ítems de la orden
			checkoutRequest.getCart().forEach(cartItem -> {
				// Buscar el producto por nombre (puedes hacer la búsqueda según el nombre o id)
				Producto producto = productoRepository.findByProductName(cartItem.getName())
						.orElseThrow(() -> new RuntimeException("Producto no encontrado: " + cartItem.getName()));

				// Crear el OrderItem con el producto encontrado
				OrderItem item = new OrderItem(producto, cartItem.getQuantity(), cartItem.getPrice(), order);
				order.addItem(item); // Añadir el ítem a la orden
			});

			// Guardar la orden
			orderService.saveOrder(order);

			// Cargar la plantilla HTML desde los recursos
			InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/email-template.html");
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
				    .append("<td style='padding:8px; border:1px solid #444;'>").append(item.getProducto().getProductName()).append("</td>")
				    .append("<td style='padding:8px; border:1px solid #444;'>").append(item.getQuantity()).append("</td>")
				    .append("<td style='padding:8px; border:1px solid #444;'>$").append(formatoMoneda.format(subtotalItem)).append("</td>")
				    .append("</tr>");

			}

			// Fila final de total
			tablaProductos.append("<tr style='background-color:#2e7d32; color:#fff; font-weight:bold;'>")
			    .append("<td colspan='2' style='padding:8px; border:1px solid #444;'>Total</td>")
			    .append("<td style='padding:8px; border:1px solid #444;'>$").append(formatoMoneda.format(totalGeneral)).append("</td>")
			    .append("</tr>");


			// Reemplazar datos dinámicos en la plantilla
			String emailHTMLConProductos = template
			        .replace("{NOMBRE}", user.getFullName())
			        .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
			        .replace("{TOTAL}", "$" + order.getTotal())
			        .replace("{LISTADO_PRODUCTOS}", tablaProductos.toString());
			// Intentar enviar el correo
			try {
				logger.info("➡️ Enviando correo a {}", user.getEmail());
				emailService.enviarCorreoHTML(user.getEmail(), "Confirmación de Compra", emailHTMLConProductos);
			} catch (Exception e) {
				logger.error("Error al enviar el correo: ", e);
			}

			// Desactivar WhatsApp por ahora
			// whatsappService.enviarMensajeWhatsapp(user.getPhone(), mensaje);

			logger.debug("Orden creada exitosamente para el usuario: {}", user.getEmail());

			return ResponseEntity.status(HttpStatus.CREATED)
				.body(Map.of("success", true, "message", "¡Orden exitosa!, favor de revisar su correo electrónico."));
	} catch (Exception e) {
			logger.error("Error inesperado al procesar la orden: ", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("success", false, "message", "Error inesperado al procesar la orden"));
		}
		
	}
    //whatsapp
	
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getFieldErrors()
				.forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("success", false, "errors", errors));
	}
	
	

}
