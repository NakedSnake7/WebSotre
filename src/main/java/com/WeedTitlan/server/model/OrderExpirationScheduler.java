package com.WeedTitlan.server.model;

//Clase Scheduler
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.WeedTitlan.server.service.OrderService;


@Component
public class OrderExpirationScheduler {

 private final OrderService orderService;

 public OrderExpirationScheduler(OrderService orderService) {
     this.orderService = orderService;
 }

 // Cada 30 min
 @Scheduled(fixedRate = 1800000)
 public void verificarOrdenesPendientes() {
     orderService.findAllOrders().forEach(orderService::expirarOrdenSiPendiente);
 }
}
