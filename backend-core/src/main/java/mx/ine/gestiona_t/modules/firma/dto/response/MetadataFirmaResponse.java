package mx.ine.gestiona_t.modules.firma.dto.response;

import mx.ine.gestiona_t.modules.firma.model.enums.NivelFirma;
import java.time.LocalDateTime;

public record MetadataFirmaResponse(
    String folioDocumento,
    NivelFirma nivelFirma,
    String ipOrigen,
    String userAgent,
    String geolocalizacion,
    String dispositivoId,
    Double scoreCoincidenciaBiometrica,
    String certificadoSubject,
    LocalDateTime certificadoValidoHasta,
    LocalDateTime fechaFirma
) {}