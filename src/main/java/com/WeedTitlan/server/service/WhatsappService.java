package com.WeedTitlan.server.service;

import com.WeedTitlan.server.model.MensajePendiente;
import com.WeedTitlan.server.repository.MensajePendienteRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class WhatsappService {

    private static final Logger logger = LoggerFactory.getLogger(WhatsappService.class);

    @Autowired
    private MensajePendienteRepository mensajePendienteRepository;

    public void enviarMensajeWhatsapp(String telefono, String mensaje) {
        logger.info("Intentando enviar WhatsApp a: {}", telefono);

        String apiUrl = "https://whatsapp-bot-kxd6.onrender.com/send-message";
        int maxReintentos = 3;

        for (int intento = 1; intento <= maxReintentos; intento++) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                String json = String.format("{\"phone\":\"%s\",\"message\":\"%s\"}",
                        telefono, mensaje.replace("\"", "'"));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl))
                        .header("Content-Type", "application/json")
                        .timeout(java.time.Duration.ofSeconds(10))
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                int statusCode = response.statusCode();

                logger.info("Respuesta WhatsApp ({}): {}", statusCode, response.body());

                if (statusCode == 200) {
                    logger.info("✅ WhatsApp enviado con éxito a {}", telefono);
                    return;
                } else {
                    logger.warn("⚠️ Fallo al enviar WhatsApp ({}), intento {}", statusCode, intento);
                }

            } catch (Exception e) {
                logger.error("❌ Error en intento {} al enviar WhatsApp a {}: {}", intento, telefono, e.getMessage());
            }

            try {
                Thread.sleep(2000); // esperar antes del siguiente intento
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        // Si no se logró enviar, lo guardamos para reintento posterior
        MensajePendiente pendiente = new MensajePendiente();
        pendiente.setTelefono(telefono);
        pendiente.setMensaje(mensaje);
        mensajePendienteRepository.save(pendiente);
        logger.warn("📦 Mensaje guardado para reenvío posterior: {}", telefono);
    }
}
