package com.WeedTitlan.server.service;

import com.WeedTitlan.server.exceptions.OrderNotFoundException;     

import com.WeedTitlan.server.exceptions.ResourceNotFoundException;
import com.WeedTitlan.server.dto.CheckoutRequestDTO;
import com.WeedTitlan.server.dto.OrderItemDTO;
import com.WeedTitlan.server.dto.OrderRequestDTO;
import com.WeedTitlan.server.exceptions.InsufficientStockException;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.Order.PaymentMethod;
import com.WeedTitlan.server.model.OrderItem;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.PaymentStatus;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public Optional<Order> findByStripeSessionId(String stripeSessionId) {
        return orderRepository.findByStripeSessionId(stripeSessionId);
    }
    
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

  


    @Transactional
    public Order saveOrder(Order order) {
        // Guardar orden
        Order saved = orderRepository.save(order);

        // 🔥 SOLO TRANSFERENCIA APARTA STOCK
        if (order.getPaymentMethod() == PaymentMethod.TRANSFER) {
            descontarStock(saved);
        }

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
            return orderRepository.findByOrderStatus(orderStatus);

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
            order.setOrderStatus(status);
            Order updatedOrder = orderRepository.save(order);

            // ✅ Enviar correo automático según el status
            try {
            	if (status == OrderStatus.PROCESSED) {
            		emailService.enviarCorreoPedidoProcesado(
            			    order.getUser().getEmail(),
            			    order.getUser().getFullName(),
            			    order.getId(),
            			    order.getItems()
            			);

                    // Marcar email como enviado
                    order.setEmailSent(true);
                    orderRepository.save(order);

                } else if (status == OrderStatus.SHIPPED) {
                    // Correo de "pedido enviado" (con tracking y paquetería)
                    emailService.enviarCorreoEnvio(
                            order.getUser().getEmail(),
                            order.getUser().getFullName(),
                            order.getId(),
                            java.time.LocalDate.now().toString(), // fecha de envío
                            order.getTrackingNumber(),
                            order.getCarrier()
                    );
                }
            } catch (IOException e) {
                System.err.println("Error enviando correo: " + e.getMessage());
            }

            return updatedOrder;

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado no válido: " + newStatus);
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
    public void expirarOrdenSiPendiente(Order order) {

        // 1. Solo órdenes CREADAS pueden expirar
        if (order.getOrderStatus() != OrderStatus.CREATED) return;

        // 2. Solo transferencias tienen vencimiento de 24h
        if (order.getPaymentMethod() != PaymentMethod.TRANSFER) return;
        
        if (order.getPaidAt() != null || order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }


        LocalDateTime limite = order.getOrderDate().plusHours(24);

        // 3. Aún no expira
        if (LocalDateTime.now().isBefore(limite)) return;
        
        

        // ============================
        // 4. RESTAURAR STOCK (SAFE)
        // ============================
        if (order.isStockReduced()) {

            for (OrderItem item : order.getItems()) {

                Producto producto = productoRepository
                        .findByIdForUpdate(item.getProducto().getId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Producto no encontrado al restaurar stock"
                                )
                        );

                int cantidad = item.getQuantity();

                if (cantidad > 0) {
                    producto.setStock(producto.getStock() + cantidad);
                    productoRepository.save(producto);
                }
            }

            // 🔄 Marcar como stock restaurado
            order.setStockReduced(false);
        }

        // ============================
        // 5. MARCAR ORDEN COMO CANCELADA
        // ============================
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // ============================
        // 6. EVITAR CORREO DUPLICADO
        // ============================
        if (order.getExpirationEmailSent()) return;

        // ============================
        // 7. ENVIAR CORREO (EmailService)
        // ============================
        try {
            emailService.enviarCorreoOrdenExpirada(order, limite);

            order.setExpirationEmailSent(true);
            orderRepository.save(order);

        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de expiración: " + e.getMessage());
        }
        
    }

    
    
    @Transactional
    public void validarStockCheckout(CheckoutRequestDTO request) {

        if (request.getCart() == null || request.getCart().isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío");
        }

        request.getCart().forEach(item -> {
            Producto producto = productoRepository
                    .findByIdForUpdate(item.getProductId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Producto no encontrado: " + item.getProductId()
                            ));

            if (producto.getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        "Stock insuficiente para " + producto.getProductName() +
                        ". Disponibles: " + producto.getStock()
                );
            }
        });
    }
    @Transactional
    public void confirmarPagoTransferencia(Order order) {

        // ============================
        // 0. VALIDACIONES
        // ============================
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("La orden está expirada");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return; // ya confirmada
        }

        // ============================
        // 1. ACTUALIZAR ESTADOS
        // ============================
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setOrderStatus(OrderStatus.PROCESSED);
        order.setPaidAt(LocalDateTime.now()); // ✅ AHORA SÍ

        orderRepository.save(order);

        // ============================
        // 2. ENVIAR CORREO
        // ============================
        if (order.getUser() == null || order.getUser().getEmail() == null) return;

        try {
            emailService.enviarCorreoPedidoProcesado(
                    order.getUser().getEmail(),
                    order.getCustomerName(),
                    order.getId(),
                    order.getItems()
            );
        } catch (Exception e) {
            System.err.println("❌ Error enviando correo de pago confirmado: " + e.getMessage());
        }
    }




    
}
