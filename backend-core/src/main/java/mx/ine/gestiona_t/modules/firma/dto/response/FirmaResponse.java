package mx.ine.gestiona_t.modules.firma.dto.response;

import mx.ine.gestiona_t.modules.firma.model.enums.EstatusFirma;
import mx.ine.gestiona_t.modules.firma.model.enums.NivelFirma;
import java.time.LocalDateTime;
import java.util.UUID;

public record FirmaResponse(
    UUID id,
    String folioDocumento,
    String folioAspirante,
    NivelFirma nivelFirma,
    EstatusFirma estatus,
    String nombreArchivo,
    String hashOriginal,
    String hashFirmado,
    LocalDateTime fechaSolicitud,
    LocalDateTime fechaFirma,
    String mensaje
) {}