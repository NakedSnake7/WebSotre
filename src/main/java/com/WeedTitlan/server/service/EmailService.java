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
    private String fromEmail;

    @Value("${sendgrid.from.name:WeedTlan}")
    private String fromName;

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

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(destinatario);
        Content content = new Content("text/html", htmlCuerpo);
        Mail mail = new Mail(from, asunto, to, content);

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
}
