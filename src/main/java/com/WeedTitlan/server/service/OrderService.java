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

    public OrderService(OrderRepository orderRepository, ProductoRepository productoRepository) {
        this.orderRepository = orderRepository;
        this.productoRepository = productoRepository;
    }

    // Guardar una orden con sus productos
    @Transactional
    public Order saveOrderWithItems(Order order, List<OrderItem> orderItems) {
        for (OrderItem item : orderItems) {
            item.setOrder(order);
            order.addItem(item);
        }
        return orderRepository.save(order);
    }

    // Guardar una orden sin productos
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
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            return orderRepository.findByStatus(orderStatus);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Estado de orden no válido: " + status, e);
        }
    }

    // Actualizar el estado de una orden y manejar stock
    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();

            try {
                OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());

                if (status == OrderStatus.DELIVERED) {
                    restarStock(order);
                }

                order.setStatus(status);
                return orderRepository.save(order);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Estado de orden no válido: " + newStatus);
            }
        } else {
            throw new OrderNotFoundException("Orden no encontrada con ID: " + orderId);
        }
    }

    // Restar stock de los productos cuando la orden se confirme
    private void restarStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Producto producto = item.getProducto(); 
            int cantidadComprada = item.getQuantity();

            if (producto.getStock() < cantidadComprada) {
                throw new InsufficientStockException("Stock insuficiente para el producto: " + producto.getProductName());
            }

            producto.setStock(producto.getStock() - cantidadComprada);
            productoRepository.save(producto);
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
