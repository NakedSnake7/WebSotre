package com.WeedTitlan.server.service;

import java.util.List; 

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.repository.OrderRepository;

@Service
public class StockScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;

    public StockScheduler(OrderRepository orderRepository, OrderService orderService) {
        this.orderRepository = orderRepository;
        this.orderService = orderService;
    }

    // ⏱ cada 1 hora
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void revisarOrdenesPendientes() {

        List<Order> ordenes = orderRepository.findPendingOrdersWithItems();

        for (Order order : ordenes) {
            orderService.expirarOrdenSiPendiente(order);
        }
    }

}
