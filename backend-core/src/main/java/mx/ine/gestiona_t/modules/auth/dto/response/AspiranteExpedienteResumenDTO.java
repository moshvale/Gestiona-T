package mx.ine.gestiona_t.modules.auth.dto.response;

import java.util.UUID;

public record AspiranteExpedienteResumenDTO(
    UUID id,
    String folio,
    String nombreCompleto,
    String correoElectronico,
    String estatusGeneral,
    int documentosTotales,
    int documentosValidados,
    int documentosRechazados
) {}
