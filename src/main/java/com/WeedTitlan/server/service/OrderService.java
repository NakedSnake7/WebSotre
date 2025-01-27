package com.WeedTitlan.server.service;

import com.WeedTitlan.server.exceptions.OrderNotFoundException;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderItem;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.repository.OrderRepository;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Guardar una orden con sus productos
    @Transactional
    public Order saveOrderWithItems(Order order, List<OrderItem> orderItems) {
        // Asocia los ítems con la orden
        for (OrderItem item : orderItems) {
            item.setOrder(order); // Establecer la relación
            order.addItem(item); // Agregar el ítem a la orden
        }

        // Guardar la orden (JPA se encargará de guardar también los ítems relacionados)
        return orderRepository.save(order);
    }

    // Guardar una orden sin productos (si se necesita)
    @Transactional
    public Order saveOrder(Order order) {
        return orderRepository.save(order);
    }

    // Buscar una orden por su ID
    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // Buscar todas las órdenes
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // Buscar órdenes por estado
    public List<Order> findOrdersByStatus(String status) {
        try {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase()); // Convierte el String a enum
            return orderRepository.findByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de orden no válido: " + status, e);
        }
    }

    // Actualizar el estado de una orden
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            // Verificar si el nuevo estado es válido
            try {
                OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
                order.setStatus(status);
                return orderRepository.save(order);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado de orden no válido: " + newStatus);
            }
        } else {
            throw new OrderNotFoundException("Orden no encontrada con ID: " + orderId);
        }
    }

    // Eliminar una orden
    public void deleteOrder(Long orderId) {
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
        } else {
            throw new OrderNotFoundException("Orden no encontrada con ID: " + orderId);
        }
    }
}
