package com.WeedTitlan.server;

import com.WeedTitlan.server.exceptions.OrderNotFoundException;
import com.WeedTitlan.server.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // Guardar una orden
    public Order saveOrder(Order order) {
        // Aquí podrías agregar lógica de negocio adicional si es necesario
        return orderRepository.save(order);
    }

    // Buscar una orden por su ID
    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    // Buscar todas las órdenes (puedes agregar paginación si es necesario)
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // Buscar órdenes por estado
    public List<Order> findOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
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
