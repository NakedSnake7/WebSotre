package com.WeedTitlan.server.service;

import com.WeedTitlan.server.exceptions.OrderNotFoundException;
import com.WeedTitlan.server.exceptions.InsufficientStockException;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderItem;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.Producto;
import com.WeedTitlan.server.repository.OrderRepository;
import com.WeedTitlan.server.repository.ProductoRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

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
        return orderRepository.save(order);
    }

    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
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

            // Si el pedido se procesa y aún no se ha restado stock, restarlo
            if (status == OrderStatus.PROCESSED && !order.isStockReduced()) {
                restarStock(order);
                order.setStockReduced(true);
            }

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

}
