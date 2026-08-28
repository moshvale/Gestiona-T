package mx.ine.gestiona_t.modules.documentos.dto.response;

import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusRevision;
import java.time.LocalDateTime;
import java.util.UUID;

public record RevisionResponse(
    Long id,
    UUID documentoId,
    UUID analistaId,
    EstatusRevision estatus,
    String dictamen,
    String motivo,
    Integer prioridad,
    LocalDateTime fechaAsignacion,
    LocalDateTime fechaDictamen
) {}