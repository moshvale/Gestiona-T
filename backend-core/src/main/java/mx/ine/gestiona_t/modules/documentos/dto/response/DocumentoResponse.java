package mx.ine.gestiona_t.modules.documentos.dto.response;

import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.TipoValidacion;
import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentoResponse(
    UUID id,
    UUID aspiranteId,
    String folio,
    TipoDocumento tipoDocumento,
    TipoValidacion tipoValidacion,
    EstatusDocumento estatus,
    String nombreArchivo,
    Double scoreAutenticidad,
    String motivoRechazo,
    LocalDateTime fechaCarga,
    LocalDateTime fechaValidacion,
    UUID expedienteLaboralId
) {}