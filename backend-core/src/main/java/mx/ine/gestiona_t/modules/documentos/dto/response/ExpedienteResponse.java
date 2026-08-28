package mx.ine.gestiona_t.modules.documentos.dto.response;

import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusExpediente;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpedienteResponse(
    UUID id,
    String folio,
    UUID aspiranteId,
    int documentosTotales,
    int documentosValidados,
    int documentosRechazados,
    int documentosEnRevision,
    EstatusExpediente estatusGeneral,
    boolean sfpVerificado,
    Boolean sfpHabilitado,
    LocalDateTime fechaUltimaActualizacion
) {}