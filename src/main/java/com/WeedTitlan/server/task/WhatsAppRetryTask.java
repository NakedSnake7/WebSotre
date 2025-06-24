/*
 * package com.WeedTitlan.server.task;
 * 
 * import com.WeedTitlan.server.model.MensajePendiente; import
 * com.WeedTitlan.server.repository.MensajePendienteRepository; import
 * com.WeedTitlan.server.service.WhatsappService;
 * 
 * 
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.scheduling.annotation.Scheduled; import
 * org.springframework.stereotype.Component;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory;
 * 
 * 
 * import java.util.List;
 * 
 * @Component public class WhatsAppRetryTask {
 * 
 * @Autowired private MensajePendienteRepository mensajePendienteRepository;
 * 
 * 
 * @Autowired private WhatsappService whatsappService;
 * 
 * private static final Logger logger =
 * LoggerFactory.getLogger(WhatsAppRetryTask.class);
 * 
 * 
 * @Scheduled(fixedDelay = 60000) public void reenviarMensajes() {
 * List<MensajePendiente> pendientes =
 * mensajePendienteRepository.findTop10ByEnviadoFalseOrderByCreadoEnAsc();
 * 
 * for (MensajePendiente mensaje : pendientes) { try {
 * whatsappService.enviarMensajeWhatsapp(mensaje.getTelefono(),
 * mensaje.getMensaje()); mensaje.setEnviado(true);
 * mensajePendienteRepository.save(mensaje); } catch (Exception e) { // El
 * reintento falló, se deja sin cambios
 * logger.error("❌ Error al reenviar WhatsApp pendiente: {}", e.getMessage()); }
 * } } }
 */
