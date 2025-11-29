package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class OrdersController {

    private final OrderService orderService;

    @Autowired
    public OrdersController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public String listarOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Order> orders;

        // FILTRAR POR ESTADO
        if (status != null && !status.isEmpty()) {
            orders = orderService.findOrdersByStatus(status);
        } else {
            orders = orderService.findAllOrders();
        }

        // FILTRAR POR NOMBRE (en memoria)
        if (search != null && !search.isEmpty()) {
            String text = search.toLowerCase();
            orders = orders.stream()
                    .filter(o -> o.getCustomerName() != null &&
                            o.getCustomerName().toLowerCase().contains(text))
                    .toList();
        }

        model.addAttribute("orders", orders);
        return "orders";
    }
  
    
  
    @GetMapping("/orders/{id}")
    public String verDetalles(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderByIdWithUserAndItems(id);
        model.addAttribute("order", order);
        return "order-details";
    }



    @PostMapping("/orders/{id}/status")
    public String cambiarStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {

        orderService.updateOrderStatus(id, status);
        return "redirect:/orders";
    }

    @GetMapping("/orders/{id}/delete")
    public String eliminarOrden(@PathVariable("id") Long id) {
        orderService.deleteOrder(id);
        return "redirect:/orders";
    }

}
