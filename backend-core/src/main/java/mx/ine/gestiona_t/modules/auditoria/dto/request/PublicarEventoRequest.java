package mx.ine.gestiona_t.modules.auditoria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.ine.gestiona_t.modules.auditoria.model.enums.ActorTipo;
import mx.ine.gestiona_t.modules.auditoria.model.enums.CategoriaEvento;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import mx.ine.gestiona_t.modules.auditoria.model.enums.TipoEvento;
import java.util.Map;
import java.util.UUID;

public record PublicarEventoRequest(
    @NotNull CategoriaEvento categoria,
    @NotNull TipoEvento tipoEvento,
    @NotNull NivelSeveridad severidad,
    UUID actorId,
    ActorTipo actorTipo,
    String recursoAfectado,
    @NotBlank String descripcion,
    Map<String, Object> datosEvento,
    String moduloOrigen,
    String correlationId
) {}