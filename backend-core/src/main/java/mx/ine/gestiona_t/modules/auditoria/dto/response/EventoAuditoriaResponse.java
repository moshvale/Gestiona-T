package mx.ine.gestiona_t.modules.auditoria.dto.response;

import mx.ine.gestiona_t.modules.auditoria.model.enums.ActorTipo;
import mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.auditoria.model.enums.TipoEvento;
import java.time.LocalDateTime;
import java.util.UUID;

public record EventoAuditoriaResponse(
    UUID id,
    CategoriaEvento categoria,
    TipoEvento tipoEvento,
    NivelSeveridad severidad,
    UUID actorId,
    ActorTipo actorTipo,
    String ipOrigen,
    String recursoAfectado,
    String descripcion,
    String hashPropio,
    String hashAnterior,
    LocalDateTime timestamp,
    String correlationId,
    String moduloOrigen,
    boolean ancladoBlockchain
) {}