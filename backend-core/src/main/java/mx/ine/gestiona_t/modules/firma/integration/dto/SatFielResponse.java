package mx.ine.gestiona_t.modules.firma.integration.dto;

import java.time.LocalDateTime;

public record SatFielResponse(
    boolean certificadoValido,
    String serial,
    String subject,
    String rfc,
    LocalDateTime validoDesde,
    LocalDateTime validoHasta,
    String mensaje
) {}