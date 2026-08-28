package mx.ine.gestiona_t.modules.cv.dto.response;

import mx.ine.gestiona_t.modules.cv.model.enums.NivelHabilidad;
import mx.ine.gestiona_t.modules.cv.model.enums.TipoHabilidad;
import java.time.LocalDate;
import java.util.UUID;

public record HabilidadResponse(
    UUID id,
    TipoHabilidad tipo,
    String nombre,
    NivelHabilidad nivel,
    LocalDate fechaCertificacion,
    LocalDate fechaVencimiento
) {}