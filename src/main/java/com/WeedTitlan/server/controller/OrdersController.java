package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
public class OrdersController {

    private final OrderService orderService;
    private final EmailService emailService;

    @Autowired
    public OrdersController(OrderService orderService, EmailService emailService) {
        this.orderService = orderService;
        this.emailService = emailService;
    }
 // ================================
    // LISTAR ÓRDENES
    // ================================
    @PostMapping("/orders/update-shipping")
    public String updateShipping(
            @RequestParam("orderId") Long orderId,
            @RequestParam("courier") String courier,
            @RequestParam("trackingNumber") String trackingNumber,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.updateShippingInfo(orderId, trackingNumber, courier);
            redirectAttributes.addFlashAttribute("success", "Datos de envío actualizados correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar los datos de envío");
        }

        return "redirect:/orders/" + orderId; // vuelve a la página de detalles
    }

    // ================================
    // LISTAR ÓRDENES
    // ================================
    @GetMapping("/orders")
    public String listarOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Order> orders;

        if (status != null && !status.isEmpty()) {
            orders = orderService.findOrdersByStatus(status);
        } else {
            orders = orderService.findAllOrders();
        }

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

    // ================================
    // DETALLES
    // ================================
    @GetMapping("/orders/{id}")
    public String verDetalles(@PathVariable("id") Long id, Model model) {
        Order order = orderService.getOrderByIdWithUserAndItems(id);
        model.addAttribute("order", order);
        return "order-details";
    }

    // ================================
    // CAMBIAR STATUS
    // ================================
    @PostMapping("/orders/{id}/status")
    public String cambiarStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") String status) {

        orderService.updateOrderStatus(id, status);
        return "redirect:/orders";
    }

    // ================================
    // 🟩 ENVIAR CORREO DE ENVÍO
    // ================================
    @PostMapping("/orders/{id}/send-shipping-email")
    public String enviarCorreoEnvio(
            @PathVariable Long id,
            @RequestParam("trackingNumber") String trackingNumber,
            Model model
    ) {

        try {
            // Validación simple
            if (trackingNumber == null || trackingNumber.isBlank()) {
                model.addAttribute("error", "Debes ingresar un número de guía.");
                return "redirect:/orders/" + id;
            }

            Order order = orderService.getOrderByIdWithUserAndItems(id);

            if (order == null) {
                model.addAttribute("error", "Orden no encontrada");
                return "redirect:/orders";
            }

            // Guardar tracking y status
            order.setTrackingNumber(trackingNumber);
            order.setStatus(OrderStatus.SHIPPED);
            orderService.save(order);

            // Datos del correo
            String nombre = order.getCustomerName();
            String email = order.getUser().getEmail();
            String fechaEnvio = LocalDate.now().toString();
            String carrier = "Paquetería";

            // Enviar correo
            emailService.enviarCorreoEnvio(
                    email,
                    nombre,
                    order.getId(),
                    fechaEnvio,
                    trackingNumber,
                    carrier
            );

            return "redirect:/orders";

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al enviar correo: " + e.getMessage());
            return "redirect:/orders";
        }
    }

    // ================================
    // ELIMINAR
    // ================================
    @GetMapping("/orders/{id}/delete")
    public String eliminarOrden(@PathVariable("id") Long id) {
        orderService.deleteOrder(id);
        return "redirect:/orders";
    }
  
}
