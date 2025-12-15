package com.WeedTitlan.server.controller;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.WeedTitlan.server.dto.CheckoutRequestDTO;
import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.User;
import com.WeedTitlan.server.service.EmailService;
import com.WeedTitlan.server.service.OrderService;
import com.WeedTitlan.server.service.UserService;
import com.WeedTitlan.server.service.StripeCheckoutService;

import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;

@RestController
@RequestMapping("/api/stripe")
public class StripeCheckoutController {

    private final OrderService orderService;
    private final UserService userService;
    private final EmailService emailService;
    private final StripeCheckoutService stripeCheckoutService;


    public StripeCheckoutController(
            OrderService orderService,
            UserService userService,
            EmailService emailService,
            StripeCheckoutService stripeCheckoutService
    ) {
        this.orderService = orderService;
        this.userService = userService;
        this.emailService = emailService;
        this.stripeCheckoutService = stripeCheckoutService;
    }


    @PostMapping("/checkout")
    public ResponseEntity<?> createStripeCheckout(
            @RequestBody CheckoutRequestDTO checkoutRequest) throws Exception {

        // 1️⃣ Usuario
        User user = userService.findOrCreateUserByEmail(
                checkoutRequest.getCustomer().getEmail(),
                checkoutRequest.getCustomer().getFullName(),
                checkoutRequest.getCustomer().getPhone()
        );

        // 2️⃣ Totales
        double subtotal = checkoutRequest.getCart().stream()
                .mapToDouble(i -> i.getPrice() * i.getQuantity())
                .sum();

        double envio = subtotal >= 1250 ? 0 : 120;
        double total = subtotal + envio;

        // 3️⃣ Orden pendiente de pago
        Order order = new Order(
                user,
                total,
                OrderStatus.PENDING_PAYMENT,
                checkoutRequest.getCustomer().getAddress(),
                user.getFullName()
        );

        orderService.save(order); // ✅ solo guarda

        // 4️⃣ Stripe Checkout Session
        Session session = stripeCheckoutService.createSession(order);

     // 5️⃣ Guardar session ID
        order.setStripeSessionId(session.getId());
        orderService.save(order);
        return ResponseEntity.ok(Map.of("url", session.getUrl()));
    }

    @GetMapping("/payment-success")
    public ResponseEntity<?> success(@RequestParam("session_id") String sessionId)
            throws StripeException, IOException {

        Order order = orderService.findByStripeSessionId(sessionId)
                .orElseThrow();

        Session session = Session.retrieve(sessionId);

        if ("paid".equals(session.getPaymentStatus())) {

            if (order.getStatus() != OrderStatus.PAID) {

                order.setStatus(OrderStatus.PAID);

                // ✅ AHORA SÍ DESCUENTA STOCK
                orderService.saveOrder(order);

                emailService.enviarCorreoPedidoProcesado(
                    order.getUser().getEmail(),
                    order.getUser().getFullName(),
                    order.getId(),
                    order.getItems()
                );
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

}
