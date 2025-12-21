package com.WeedTitlan.server.controller;

import com.WeedTitlan.server.model.Order; 
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.PaymentStatus;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.EmailService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrdersController {

    private final OrderService orderService;
    public OrdersController(OrderService orderService, EmailService emailService) {
        this.orderService = orderService;
    }

    // ================================
    // LISTAR ÓRDENES
    // ================================
    @GetMapping
    public String listarOrders(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "search", required = false) String search,
            Model model) {

        List<Order> orders = (status != null && !status.isBlank())
                ? orderService.findOrdersByStatus(status)
                : orderService.findAllOrders();

        if (search != null && !search.isBlank()) {
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
    @GetMapping("/{id}")
    public String verDetalles(@PathVariable Long id, Model model) {
        Order order = orderService.getOrderByIdWithUserAndItems(id);
        model.addAttribute("order", order);
        return "order-details";
    }

    // ================================
    // CONFIRMAR PAGO (TRANSFERENCIA)
    // ================================
    @PostMapping("/{id}/confirm-payment")
    public String confirmarPagoTransferencia(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Order order = orderService.getOrderByIdWithUserAndItems(id);

        // 🔴 1. ORDEN EXPIRADA O CANCELADA
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "La orden está expirada y el stock fue liberado"
            );
            return "redirect:/orders/" + id;
        }

        // 🟡 2. YA PAGADA
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute(
                    "info",
                    "La orden ya había sido confirmada previamente"
            );
            return "redirect:/orders/" + id;
        }

        // 🟢 3. CONFIRMAR PAGO (SERVICE)
        orderService.confirmarPagoTransferencia(order);

        redirectAttributes.addFlashAttribute(
                "success",
                "Pago confirmado correctamente. Orden aprobada."
        );

        return "redirect:/orders/" + id;
    }


    // ================================
    // ACTUALIZAR ENVÍO
    // ================================
    @PostMapping("/update-shipping")
    public String updateShipping(
            @RequestParam Long orderId,
            @RequestParam String courier,
            @RequestParam String trackingNumber,
            RedirectAttributes redirectAttributes) {

        try {
            orderService.updateShippingInfo(orderId, trackingNumber, courier);
            orderService.updateOrderStatus(orderId, "SHIPPED");
            redirectAttributes.addFlashAttribute("success", "Envío actualizado");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar envío");
        }

        return "redirect:/orders/" + orderId;
    }

    // ================================
    // ELIMINAR ORDEN
    // ================================
    @GetMapping("/{id}/delete")
    public String eliminarOrden(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        Order order = orderService.getOrderById(id);

        if (order.getPaymentStatus() == PaymentStatus.PAID ||
            order.getOrderStatus() == OrderStatus.CANCELLED) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No se puede eliminar esta orden"
            );
            return "redirect:/orders";
        }

        orderService.deleteOrder(id);
        redirectAttributes.addFlashAttribute("success", "Orden eliminada");

        return "redirect:/orders";
    }
}
