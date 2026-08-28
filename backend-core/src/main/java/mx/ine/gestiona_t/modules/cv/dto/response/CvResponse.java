package mx.ine.gestiona_t.modules.cv.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CvResponse(
    UUID id,
    String folio,
    UUID aspiranteId,
    int scoreCompletitud,
    boolean completo,
    String metodoCaptura,
    LocalDateTime fechaCaptura,
    LocalDateTime fechaUltimaModificacion
) {}