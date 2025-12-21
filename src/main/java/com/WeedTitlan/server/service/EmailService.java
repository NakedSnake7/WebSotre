package com.WeedTitlan.server.service;

import com.WeedTitlan.server.model.Order;
import com.WeedTitlan.server.model.OrderItem;
import com.sendgrid.*; 
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.List;

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
    public void enviarCorreoPagoConfirmado(
            String destinatario,
            String nombre,
            Long orderId,
            List<OrderItem> items
    ) throws IOException {

        String template = cargarTemplate("email/order-processed.html");

        StringBuilder listadoProductos = new StringBuilder();

        for (OrderItem item : items) {
            listadoProductos.append("<tr>")
                    .append("<td>").append(item.getProducto().getProductName()).append("</td>")
                    .append("<td style='text-align:center;'>").append(item.getQuantity()).append("</td>")
                    .append("</tr>");
        }

        String html = template
                .replace("{NOMBRE}", nombre)
                .replace("{NUMERO_ORDEN}", String.valueOf(orderId))
                .replace("{LISTADO_PRODUCTOS}", listadoProductos.toString());

        enviarCorreoHTML(
                destinatario,
                "✅ Pago confirmado - Pedido #" + orderId,
                html
        );
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

 
    public void enviarCorreoPedidoProcesado(String destinatario, String nombre, Long orderId, List<OrderItem> items) throws IOException {
        // 1. Cargar template
        String template = cargarTemplate("email/order-processed.html");

        // 2. Configurar formato de moneda
        DecimalFormat df = new DecimalFormat("#,##0.00");

        // 3. Construir tabla de productos
        StringBuilder listadoProductos = new StringBuilder();
        double subtotal = 0;
        for (OrderItem item : items) {
            double sub = item.getPrice() * item.getQuantity();
            subtotal += sub;
            listadoProductos.append("<tr style='background-color:#2a2a2a; color:#fff;'>")
                .append("<td style='padding:10px; border-bottom:1px solid #333;'>")
                .append(item.getProducto().getProductName())
                .append("</td>")
                .append("<td style='padding:10px; text-align:center; border-bottom:1px solid #333;'>")
                .append(item.getQuantity())
                .append("</td>")
                .append("<td style='padding:10px; text-align:center; border-bottom:1px solid #333;'>$")
                .append(df.format(sub))
                .append("</td>")
                .append("</tr>");
        }

        // 4. Calcular envío
        String envio = subtotal >= 1250 ? "GRATIS" : "$120.00";
        double total = subtotal + (subtotal >= 1250 ? 0 : 120);

        // 5. Reemplazar placeholders en el template
        String emailHTML = template
            .replace("{NOMBRE}", nombre)
            .replace("{NUMERO_ORDEN}", String.valueOf(orderId))
            .replace("{TOTAL}", df.format(total))
            .replace("{LISTADO_PRODUCTOS}", listadoProductos.toString())
            .replace("{ENVIO}", envio)
            .replace("{SUBTOTAL}", df.format(subtotal));

        // 6. Enviar correo
        enviarCorreoHTML(destinatario, "✅ Confirmación de tu pedido #" + orderId, emailHTML);
    }
    
    public void enviarCorreoOrdenExpirada(Order order, LocalDateTime fechaLimite) throws IOException {

        if (order.getUser() == null || order.getUser().getEmail() == null) return;

        String template = cargarTemplate("email/email-order-expired.html");

        DecimalFormat df = new DecimalFormat("#,##0.00");

        StringBuilder tablaProductos = new StringBuilder();
        double subtotal = 0;

        for (OrderItem item : order.getItems()) {
            double sub = item.getPrice() * item.getQuantity();
            subtotal += sub;

            tablaProductos.append("<tr>")
                    .append("<td style='padding:10px;'>")
                    .append(item.getProducto() != null
                            ? item.getProducto().getProductName()
                            : "Producto")
                    .append("</td>")
                    .append("<td style='text-align:center;'>")
                    .append(item.getQuantity())
                    .append("</td>")
                    .append("<td style='text-align:center;'>$")
                    .append(df.format(sub))
                    .append("</td>")
                    .append("</tr>");
        }

        String envio = subtotal >= 1250 ? "GRATIS" : "$120.00";
        double total = subtotal + (subtotal >= 1250 ? 0 : 120);

        String html = template
                .replace("{NOMBRE}",
                        order.getCustomerName() != null
                                ? order.getCustomerName()
                                : "Cliente")
                .replace("{NUMERO_ORDEN}", String.valueOf(order.getId()))
                .replace("{FECHA_EXPIRACION}", fechaLimite.toString())
                .replace("{LISTADO_PRODUCTOS}", tablaProductos.toString())
                .replace("{SUBTOTAL}", df.format(subtotal))
                .replace("{ENVIO}", envio)
                .replace("{TOTAL}", df.format(total));

        enviarCorreoHTML(
                order.getUser().getEmail(),
                "⏰ Orden expirada - WeedTlan",
                html
        );
    }

    /**
     * Envía el correo de "Pedido enviado" con tracking
     */
    /**
     * 📦 Correo: Pedido enviado con tracking
     */
    public void enviarCorreoEnvio(
            String destinatario,
            String customerName,
            Long orderId,
            String shippingDate,
            String trackingNumber,
            String carrier
    ) throws IOException {

        String template = cargarTemplate("email/shipping-confirmation.html");

        String html = template
                .replace("{CUSTOMER_NAME}", customerName)
                .replace("{ORDER_ID}", String.valueOf(orderId))
                .replace("{SHIPPING_DATE}", shippingDate)
                .replace("{TRACKING_NUMBER}", trackingNumber)
                .replace("{CARRIER}", carrier);

        enviarCorreoHTML(
                destinatario,
                "📦 Tu pedido está en camino - Orden #" + orderId,
                html
        );
    }



}
