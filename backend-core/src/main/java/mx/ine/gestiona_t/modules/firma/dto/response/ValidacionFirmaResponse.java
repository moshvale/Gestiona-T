package mx.ine.gestiona_t.modules.firma.dto.response;

import mx.ine.gestiona_t.modules.firma.model.enums.NivelFirma;
import java.time.LocalDateTime;

public record ValidacionFirmaResponse(
    boolean valida,
    String folioDocumento,
    NivelFirma nivelFirma,
    String hashOriginal,
    String hashFirmado,
    String autoridadTimestamp,
    LocalDateTime timestampCertificado,
    String mensaje
) {}