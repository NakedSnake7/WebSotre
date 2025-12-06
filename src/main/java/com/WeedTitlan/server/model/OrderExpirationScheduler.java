package com.WeedTitlan.server.model;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.repository.OrderRepository;

@Component
public class OrderExpirationScheduler {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    // Protección anti-ejecuciones dobles
    private boolean running = false;

    public OrderExpirationScheduler(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    // Cada 30 min
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public synchronized void verificarOrdenesPendientes() {

        // Evitar múltiple ejecución concurrente
        if (running) {
            System.out.println("⏳ Scheduler ya está corriendo, se omite ejecución.");
            return;
        }

        running = true;

        try {
            System.out.println("🔎 Ejecutando scheduler: buscando órdenes pendientes...");
            
            orderRepository.findPendingOrdersWithItems()
                           .forEach(orderService::expirarOrdenSiPendiente);

        } finally {
            running = false;
            System.out.println("✔️ Scheduler finalizado.");
        }
    }
}
