package com.WeedTitlan.server.controller;

import java.util.Map; 

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.PaymentStatus;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.StripeCheckoutService;

import com.stripe.model.checkout.Session;

@RestController
@RequestMapping("/api/stripe")
public class StripeCheckoutController {

    private final OrderService orderService;
    private final StripeCheckoutService stripeCheckoutService;

    public StripeCheckoutController(
            OrderService orderService,
            StripeCheckoutService stripeCheckoutService
    ) {
        this.orderService = orderService;
        this.stripeCheckoutService = stripeCheckoutService;
    }

    @PostMapping("/create-session/{orderId}")
    public ResponseEntity<?> createStripeSession(@PathVariable Long orderId) throws Exception {

    	Order order = orderService.getOrderById(orderId);


        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "La orden ya fue procesada"));
        }
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "La orden ya no está disponible para pago"));
        }

        Session session = stripeCheckoutService.createSession(order);

        order.setStripeSessionId(session.getId());
        orderService.saveOrder(order);

        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }
}
