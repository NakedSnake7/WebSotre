package com.WeedTitlan.server.service;

import com.WeedTitlan.server.exceptions.OrderNotFoundException; 
import com.WeedTitlan.server.exceptions.ResourceNotFoundException;
import com.WeedTitlan.server.dto.OrderItemDTO;
import com.WeedTitlan.server.dto.OrderRequestDTO;
import com.WeedTitlan.server.exceptions.InsufficientStockException;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderItem;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.repository.OrderRepository;
import com.WeedTitlan.server.repository.ProductoRepository;

import jakarta.transaction.Transactional;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductoRepository productoRepository;
    private final EmailService emailService; 
    
    public OrderService(OrderRepository orderRepository,
            ProductoRepository productoRepository,
            EmailService emailService) {
this.orderRepository = orderRepository;
this.productoRepository = productoRepository;
this.emailService = emailService;
}

    // ============================
    // GUARDAR ORDEN COMPLETA
    // ============================
    @Transactional
    public Order saveOrderWithItems(Order order, List<OrderItem> orderItems) {

        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.addItem(item);
        }

        // Primero guardar la orden
        Order saved = orderRepository.save(order);

        // Ahora sí descontar el stock
        descontarStock(saved);

        return saved;
    }


    @Transactional
    public Order saveOrder(Order order) {
        // Guardar orden
        Order saved = orderRepository.save(order);

        // Descontar stock
        descontarStock(saved);
        
        

        return saved;
    }


    // ============================
    // OBTENER ORDEN
    // ============================
    public Order getOrderByIdWithUser(Long id) {
        return orderRepository.findByIdWithUser(id)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada con ID: " + id));
    }

    public Order getOrderByIdWithUserAndItems(Long id) {
        return orderRepository.findByIdWithUserAndItems(id)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada con ID: " + id));
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAllWithUser();
    }

    // ============================
    // BUSCAR POR ESTADO
    // ============================
    public List<Order> findOrdersByStatus(String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + status);
        }
    }

    // ============================
    // ACTUALIZAR ESTADO + STOCK
    // ============================
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {

        Order order = getOrderByIdWithUserAndItems(orderId);

        try {
            OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());

            

            order.setStatus(status);
            Order updatedOrder = orderRepository.save(order);

            // Si cambia a SHIPPED, enviar email automáticamente usando tu método
            if (status == OrderStatus.SHIPPED) {
                emailService.enviarCorreoEnvio(
                    order.getUser().getEmail(),
                    order.getUser().getFullName(),
                    order.getId(),
                    java.time.LocalDate.now().toString(), // fecha de envío
                    order.getTrackingNumber(),
                    order.getCarrier()
                );
            }

            return updatedOrder;

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado no válido: " + newStatus);
        } catch (IOException e) {
            throw new RuntimeException("Error al generar el correo de envío: " + e.getMessage());
        }
    }

    // RESTAR STOCK
    private void restarStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Producto producto = item.getProducto();
            int cantidad = item.getQuantity();

            if (producto.getStock() < cantidad) {
                throw new InsufficientStockException(
                        "Stock insuficiente para: " + producto.getProductName()
                );
            }

            producto.setStock(producto.getStock() - cantidad);
            productoRepository.save(producto);
        }
    }
    @Transactional
    public void descontarStock(Order order) {

        // Evitar doble descuento
        if (order.isStockReduced()) {
            return;
        }

        for (OrderItem item : order.getItems()) {

            Producto producto = productoRepository
                .findByIdForUpdate(item.getProducto().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));

            if (producto.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                    "Stock insuficiente para: " + producto.getProductName()
                );
            }

            producto.setStock(producto.getStock() - item.getQuantity());
            productoRepository.save(producto);
        }

        order.setStockReduced(true);
        orderRepository.save(order);
    }


    // ============================
    // ACTUALIZAR INFO DE ENVÍO
    // ============================
    @Transactional
    public Order updateShippingInfo(Long orderId, String trackingNumber, String carrier) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada"));

        order.setTrackingNumber(trackingNumber);
        order.setCarrier(carrier);

        return orderRepository.save(order);
    }

    // ============================
    // ELIMINAR ORDEN
    // ============================
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException("Orden no encontrada con ID: " + id);
        }
        orderRepository.deleteById(id);
    }
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
    }
    public Order save(Order order) {
        return orderRepository.save(order);
    }
    @Retryable(
    	    value = { PessimisticLockingFailureException.class, CannotAcquireLockException.class },
    	    maxAttempts = 3,
    	    backoff = @Backoff(delay = 200)
    	)
    	@Transactional
    	public void procesarOrden(OrderRequestDTO request) {
    	    if (request.getItems() == null || request.getItems().isEmpty()) {
    	        throw new IllegalArgumentException("No hay items en la orden");
    	    }

    	    for (OrderItemDTO item : request.getItems()) {
    	        Producto producto = productoRepository.findByIdForUpdate(item.getProductId())
    	                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado: " + item.getProductId()));

    	        if (producto.getStock() < item.getQuantity()) {
    	            throw new InsufficientStockException(
    	                    "Stock insuficiente para " + producto.getProductName() +
    	                            ". Disponibles: " + producto.getStock());
    	          }
    	        }
    	}
    @Transactional
    public void restaurarStockSiExpirado(Order order) {

        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        LocalDateTime limite = order.getOrderDate().plusHours(24);

        if (LocalDateTime.now().isBefore(limite)) {
            return;
        }

        // Solo restaurar si realmente se descontó
        if (order.isStockReduced()) {
            for (OrderItem item : order.getItems()) {
                Producto producto = item.getProducto();
                producto.setStock(producto.getStock() + item.getQuantity());
                productoRepository.save(producto);
            }
        }

        order.setStatus(OrderStatus.EXPIRED);
        order.setStockReduced(false); // restablecer
        orderRepository.save(order);

        System.out.println("✔ Orden " + order.getId() + " expirada y stock restaurado.");
    }

    @Transactional
    public void expirarOrdenSiPendiente(Order order) {
        // Solo procesar si la orden sigue PENDING
        if (order.getStatus() != OrderStatus.PENDING) return;

        LocalDateTime limite = order.getOrderDate().plusHours(24);

        // Solo expirar si ya pasó el tiempo
        if (LocalDateTime.now().isBefore(limite)) return;

        // Restaurar stock si se descontó
        if (order.isStockReduced()) {
            for (OrderItem item : order.getItems()) {
                Producto producto = item.getProducto();
                producto.setStock(producto.getStock() + item.getQuantity());
                productoRepository.save(producto);
            }
        }

        order.setStatus(OrderStatus.EXPIRED);
        order.setStockReduced(false); // resetear
        orderRepository.save(order);

        // Enviar correo de expiración
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("email/email-order-expired.html");

            if (inputStream != null) {
                String template = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                symbols.setDecimalSeparator('.');
                symbols.setGroupingSeparator(',');
                DecimalFormat formatoMoneda = new DecimalFormat("#,##0.00", symbols);

                // Construir tabla de productos
                StringBuilder tablaProductos = new StringBuilder();
                double subtotal = 0;
                for (OrderItem item : order.getItems()) {
                    double sub = item.getPrice() * item.getQuantity();
                    subtotal += sub;

                    tablaProductos.append("<tr>")
                        .append("<td style='padding:10px; border-bottom:1px solid #2f2f2f;'>")
                        .append("<img src='").append(item.getProducto().getImageUrl()).append("' width='50' height='50' style='border-radius:6px; vertical-align:middle; margin-right:10px;'>")
                        .append(item.getProducto().getProductName())
                        .append("</td>")
                        .append("<td style='padding:10px; text-align:center; border-bottom:1px solid #2f2f2f;'>").append(item.getQuantity()).append("</td>")
                        .append("<td style='padding:10px; text-align:center; border-bottom:1px solid #2f2f2f;'>$").append(formatoMoneda.format(sub)).append("</td>")
                        .append("</tr>");
                }

                String envio = subtotal >= 1250 ? "GRATIS" : "$120.00";
                double total = subtotal + (subtotal >= 1250 ? 0 : 120);

                String emailHTML = template
                        .replace("{NOMBRE}", order.getCustomerName())
                        .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
                        .replace("{FECHA_EXPIRACION}", order.getOrderDate().plusHours(24).toString())
                        .replace("{LISTADO_PRODUCTOS}", tablaProductos.toString())
                        .replace("{SUBTOTAL}", formatoMoneda.format(subtotal))
                        .replace("{ENVIO}", envio)
                        .replace("{TOTAL}", formatoMoneda.format(total));

                emailService.enviarCorreoHTML(order.getUser().getEmail(), "Orden Expirada - WeedTlan", emailHTML);
            }
        } catch (Exception e) {
            System.err.println("Error enviando correo de expiración: " + e.getMessage());
        }
    }

    
}
