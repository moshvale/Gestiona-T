package mx.ine.gestiona_t.modules.cv.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record CursoResponse(
    UUID id,
    String nombreCurso,
    String institucion,
    int duracionHoras,
    LocalDate fechaRealizacion,
    String documentoSoportePath
) {}