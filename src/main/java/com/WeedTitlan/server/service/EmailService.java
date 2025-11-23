package com.WeedTitlan.server.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${sendgrid.api.key:}")
    private String sendgridApiKey;

    @Value("${sendgrid.from.email:}")
    private String fromEmail;

    @Value("${sendgrid.from.name:WeedTlan}")
    private String fromName;

    public void enviarCorreoHTML(String destinatario, String asunto, String htmlCuerpo) throws MessagingException, IOException {

        // Si SendGrid está configurado → usarlo
        if (sendgridApiKey != null && !sendgridApiKey.isEmpty()) {
            try {
                enviarConSendGrid(destinatario, asunto, htmlCuerpo);
                return;
            } catch (Exception e) {
                // fallback a Gmail
                enviarConGmail(destinatario, asunto, htmlCuerpo);
                return;
            }
        }

        // Si no hay SendGrid, usar Gmail
        enviarConGmail(destinatario, asunto, htmlCuerpo);
    }

    private void enviarConGmail(String destinatario, String asunto, String htmlCuerpo) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(htmlCuerpo, true);
        helper.setFrom("weedtlan.customer.service@gmail.com");

        mailSender.send(mensaje);
    }

    private void enviarConSendGrid(String destinatario, String asunto, String htmlCuerpo) throws IOException {
        if (fromEmail == null || fromEmail.isBlank()) {
            throw new IllegalStateException("SENDGRID_FROM_EMAIL está vacío o no definido.");
        }

        Email from = new Email(fromEmail, fromName);
        Email to = new Email(destinatario);
        Content content = new Content("text/html", htmlCuerpo);
        Mail mail = new Mail(from, asunto, to, content);

        SendGrid sg = new SendGrid(sendgridApiKey);

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);
        if (response.getStatusCode() >= 400) {
            throw new IOException("Error enviando correo: " + response.getStatusCode() + " - " + response.getBody());
        }
    }
}
