package mx.ine.gestiona_t.modules.auditoria.service;

import mx.ine.gestiona_t.modules.auditoria.dto.request.PublicarEventoRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Publisher asincrono de eventos de auditoria.
 * Usa @Async para no afectar el performance de los modulos origen.
 */
@Service
public class AuditoriaEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(AuditoriaEventPublisher.class);
    
    private final AuditoriaService auditoriaService;
    
    public AuditoriaEventPublisher(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }
    
    @Async("auditTaskExecutor")
    public void publicarAsincrono(PublicarEventoRequest request, String ip, String userAgent) {
        try {
            log.debug("Publicando evento asincrono: {} - {}", request.categoria(), request.tipoEvento());
            auditoriaService.publicarEvento(request, ip, userAgent);
        } catch (Exception e) {
            log.error("Error publicando evento de auditoria: {}", e.getMessage(), e);
        }
    }
    
    public void publicarSincrono(PublicarEventoRequest request, String ip, String userAgent) {
        try {
            auditoriaService.publicarEvento(request, ip, userAgent);
        } catch (Exception e) {
            log.error("Error publicando evento sincrono: {}", e.getMessage(), e);
            throw e;
        }
    }
}