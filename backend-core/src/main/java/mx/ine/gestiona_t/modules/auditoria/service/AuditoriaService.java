package mx.ine.gestiona_t.modules.auditoria.service;

import mx.ine.gestiona_t.modules.auditoria.dto.request.BuscarEventosRequest;
import mx.ine.gestiona_t.modules.auditoria.dto.request.PublicarEventoRequest;
import mx.ine.gestiona_t.modules.auditoria.dto.response.*;
import org.springframework.data.domain.Page;
import java.util.UUID;

public interface AuditoriaService {
    
    void publicarEvento(PublicarEventoRequest request, String ip, String userAgent);
    
    Page<EventoAuditoriaResponse> buscarEventos(BuscarEventosRequest request);
    
    EventoAuditoriaResponse obtenerEvento(UUID id);
    
    Page<EventoAuditoriaResponse> obtenerEventosActor(UUID actorId, int pagina, int tamano);
    
    ResumenReporteResponse generarResumen(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
    
    EstadisticasResponse obtenerEstadisticas();
    
    VerificacionIntegridadResponse verificarIntegridadCadena();
    
    byte[] exportarExcel(java.time.LocalDateTime desde, java.time.LocalDateTime hasta);
}