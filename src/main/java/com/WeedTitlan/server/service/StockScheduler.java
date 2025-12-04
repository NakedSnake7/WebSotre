package com.WeedTitlan.server.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.repository.OrderRepository;

@Service
public class StockScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public StockScheduler(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 3600000) // cada 1 hora
    public void revisarOrdenesPendientes() {

        List<Order> pendientes = orderRepository.findByStatus(OrderStatus.PENDING);

        for (Order order : pendientes) {
            orderService.restaurarStockSiExpirado(order);
        }
    }
}
