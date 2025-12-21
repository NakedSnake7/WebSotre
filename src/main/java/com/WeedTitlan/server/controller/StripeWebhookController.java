package com.WeedTitlan.server.controller;

import java.io.IOException;    

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderStatus;
import com.WeedTitlan.server.model.PaymentStatus;
import com.WeedTitlan.server.service.EmailService;
import com.WeedTitlan.server.service.OrderService;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

@RestController
@RequestMapping("/api/stripe")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    private final OrderService orderService;
    private final EmailService emailService;

    public StripeWebhookController(
            OrderService orderService,
            EmailService emailService
    ) {
        this.orderService = orderService;
        this.emailService = emailService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {

        Event event;

        try {
            event = Webhook.constructEvent(
                    payload,
                    sigHeader,
                    endpointSecret
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        switch (event.getType()) {

            case "checkout.session.completed" -> {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                handleCheckoutCompleted(session);
            }

            case "checkout.session.expired" -> {
                Session session = (Session) event.getDataObjectDeserializer()
                        .getObject()
                        .orElseThrow();

                orderService.findByStripeSessionId(session.getId())
                        .ifPresent(orderService::expirarOrdenSiPendiente);
            }
            
            case "payment_intent.payment_failed" -> {
                // log, analytics, o dejar en PENDING
            }

        }

        return ResponseEntity.ok("OK");
    }

    private void handleCheckoutCompleted(Session session) {

        Order order = orderService.findByStripeSessionId(session.getId())
                .orElseThrow();

        // 🔐 Seguridad: si el pago ya fue procesado, salir
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        // 1️⃣ Marcar pago como confirmado
        order.setPaymentStatus(PaymentStatus.PAID);

        // 2️⃣ Avanzar estado de la orden
        order.setOrderStatus(OrderStatus.PROCESSED);

        // 3️⃣ Descontar stock SOLO UNA VEZ
        orderService.descontarStock(order);

        // 4️⃣ Guardar cambios
        orderService.save(order);

        // 5️⃣ Enviar correo de confirmación
        try {
            emailService.enviarCorreoPedidoProcesado(
                    order.getUser().getEmail(),
                    order.getUser().getFullName(),
                    order.getId(),
                    order.getItems()
            );
        } catch (IOException e) {
            System.err.println("❌ Error enviando correo de pago: " + e.getMessage());
        }
    }
}
