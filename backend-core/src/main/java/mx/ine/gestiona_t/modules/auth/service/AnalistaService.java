package mx.ine.gestiona_t.modules.auth.service;

import mx.ine.gestiona_t.modules.auth.dto.request.CrearAnalistaRequest;
import mx.ine.gestiona_t.modules.auth.dto.response.AnalistaResumenDTO;
import mx.ine.gestiona_t.modules.auth.dto.response.MensajeResponse;
import java.util.List;
import java.util.UUID;

public interface AnalistaService {
    MensajeResponse crearAnalista(CrearAnalistaRequest request, UUID adminId, String ip, String userAgent);
    
    // ✅ CAMBIO: Ahora devuelve una lista de DTOs seguros, no la entidad completa
    List<AnalistaResumenDTO> listarAnalistas();
    
    MensajeResponse desactivarAnalista(UUID analistaId, UUID adminId);
}