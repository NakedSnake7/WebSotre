package com.WeedTitlan.server.service;

import com.WeedTitlan.server.model.Order;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeCheckoutService {

    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    public Session createSession(Order order) throws StripeException {

        SessionCreateParams params =
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(
                    "https://weedtitlan.com/gracias.html?session_id={CHECKOUT_SESSION_ID}"
                )
                .setCancelUrl(
                    "https://weedtitlan.com/checkout-cancel.html"
                )
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setQuantity(1L)
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency("mxn")
                                .setUnitAmount((long) (order.getTotal() * 100))
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Compra WeedTitlan")
                                        .build()
                                )
                                .build()
                        )
                        .build()
                )
                .build();

        return Session.create(params);
    }
}
