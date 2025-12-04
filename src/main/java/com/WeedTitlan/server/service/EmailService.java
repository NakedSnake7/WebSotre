package com.WeedTitlan.server.service;

import com.sendgrid.*; 
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;  // pedidos@weedtlanmx.com

    @Value("${sendgrid.from.name:WeedTlan Shops}")
    private String fromName;   // WeedTlan Ventas

    @Value("${sendgrid.reply.to}")
    private String replyToEmail; // weedtlanmx@gmail.com

    /**
     * Envía un correo HTML usando únicamente SendGrid.
     */
    public void enviarCorreoHTML(String destinatario, String asunto, String htmlCuerpo) throws IOException {

        System.out.println("🔍 [EmailService] Validando configuración SendGrid...");

        if (sendgridApiKey == null || sendgridApiKey.isBlank()) {
            throw new IllegalStateException("❌ SendGrid API Key no configurada.");
        }

        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("❌ SENDGRID_FROM_EMAIL no está configurado.");
        }

        System.out.println("✔️ API Key cargada");
        System.out.println("✔️ From Email: " + fromEmail);
        System.out.println("✔️ Reply-To: " + replyToEmail);

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(destinatario);
        Email replyTo = new Email(replyToEmail);

        Content content = new Content("text/html", htmlCuerpo);
        Mail mail = new Mail(from, asunto, to, content);

        // ➕ Agregar Reply-To real
        mail.setReplyTo(replyTo);

        SendGrid sg = new SendGrid(sendgridApiKey);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        System.out.println("📨 [EmailService] Enviando correo a: " + destinatario);

        Response response = sg.api(request);

        System.out.println("📡 SendGrid Status: " + response.getStatusCode());
        System.out.println("📡 SendGrid Body: " + response.getBody());
        System.out.println("📡 SendGrid Headers: " + response.getHeaders());

        int code = response.getStatusCode();

        if (code < 200 || code >= 300) {
            throw new IOException(
                    "❌ Error SendGrid → Código: " + code + " | Respuesta: " + response.getBody()
            );
        }
 
        System.out.println("✅ Correo enviado correctamente a " + destinatario);
    }
    /**
     * Envía el correo de "Tu pedido está en camino" usando un template HTML.
     */
    public void enviarCorreoEnvio(
            String destinatario,
            String customerName,
            Long orderId,
            String shippingDate,
            String trackingNumber,
            String carrier
    ) throws IOException {

        // 1. Cargar template desde resources
    	String html = cargarTemplate("email/shipping-confirmation.html");

        // 2. Reemplazar variables del template
        html = html.replace("${customerName}", customerName);
        html = html.replace("${orderId}", String.valueOf(orderId));
        html = html.replace("${shippingDate}", shippingDate);
        html = html.replace("${trackingNumber}", trackingNumber);
        html = html.replace("${carrier}", carrier);
        

        // 3. Enviar usando tu método existente
        enviarCorreoHTML(destinatario, "📦 Tu pedido #" + orderId + " está en camino", html);
    }

    /**
     * Lee archivos HTML desde resources como texto.
     */
    private String cargarTemplate(String path) throws IOException {
        try (var inputStream = getClass().getClassLoader().getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("❌ No se encontró el template: " + path);
            }
            return new String(inputStream.readAllBytes());
        }
    }

}
