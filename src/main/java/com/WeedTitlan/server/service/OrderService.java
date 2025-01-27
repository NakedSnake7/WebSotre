package com.WeedTitlan.server.service; 

import com.WeedTitlan.server.exceptions.OrderNotFoundException;  
import com.WeedTitlan.server.repository.OrderRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;
import java.util.Optional;
import java.util.List;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderStatus;

@Service
public class OrderService {
    
    private final OrderRepository orderRepository;
    

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    @Transactional
    public Order saveOrderWithProducts(Order order, List<String> productNames) {
        // Combinar nombres de productos en una cadena
        String concatenatedProductNames = String.join(", ", productNames);
        order.setProductNames(concatenatedProductNames);

        // Guardar la orden con los nombres de productos incluidos
        return orderRepository.save(order);
    }

    // Guardar una orden
    @Transactional
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

 // Convertir el String a OrderStatus antes de llamar al repositorio
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
