package mx.ine.gestiona_t.modules.auditoria.dto.request;

import mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import java.time.LocalDateTime;
import java.util.UUID;

public record BuscarEventosRequest(
    CategoriaEvento categoria,
    NivelSeveridad severidad,
    UUID actorId,
    String recursoAfectado,
    String moduloOrigen,
    LocalDateTime fechaDesde,
    LocalDateTime fechaHasta,
    int pagina,
    int tamanoPagina
) {
    public BuscarEventosRequest {
        if (pagina < 0) pagina = 0;
        if (tamanoPagina < 1 || tamanoPagina > 1000) tamanoPagina = 100;
    }
}