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

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductoRepository productoRepository;

    public OrderService(OrderRepository orderRepository,
                        ProductoRepository productoRepository) {
        this.orderRepository = orderRepository;
        this.productoRepository = productoRepository;
    }

    // Guardar orden con items
    @Transactional
    public Order saveOrderWithItems(Order order, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.addItem(item);
        }
        return orderRepository.save(order);
    }

    // Guardar solo la orden
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    // Obtener por ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() ->
                        new OrderNotFoundException("Orden no encontrada con ID: " + id));
    }

    // Obtener todas las órdenes
    public List<Order> findAllOrders() {
        return orderRepository.findAllWithUser();
    }


    // Buscar por estado
    public List<Order> findOrdersByStatus(String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado inválido: " + status);
        }
    }

    // Actualizar estado incluyendo manejo de stock
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {

        Order order = getOrderById(orderId);

        try {
            OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());

            // Si se entrega, bajar stock
            if (status == OrderStatus.DELIVERED) {
                restarStock(order);
            }

            order.setStatus(status);
            return orderRepository.save(order);

        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de orden no válido: " + newStatus);
        }
    }

    // Restar stock cuando se entrega la orden
    private void restarStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Producto producto = item.getProducto();
            int cantidadComprada = item.getQuantity();

            if (producto.getStock() < cantidadComprada) {
                throw new InsufficientStockException(
                        "Stock insuficiente para el producto: " + producto.getProductName()
                );
            }

            producto.setStock(producto.getStock() - cantidadComprada);
            productoRepository.save(producto);
        }
    }

    // Eliminar orden
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new OrderNotFoundException(
                    "Orden no encontrada con ID: " + id
            );
        }
        orderRepository.deleteById(id);
    }
    public Order getOrderByIdWithUser(Long id) {
        return orderRepository.findByIdWithUser(id)
                .orElseThrow(() -> new OrderNotFoundException("Orden no encontrada con ID: " + id));
    }
    public Order getOrderByIdWithUserAndItems(Long id) {
        return orderRepository.findByIdWithUserAndItems(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con ID: " + id));
    }


}
