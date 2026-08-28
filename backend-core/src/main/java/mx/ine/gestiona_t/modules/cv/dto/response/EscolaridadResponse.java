package mx.ine.gestiona_t.modules.cv.dto.response;

import mx.ine.gestiona_t.modules.cv.model.enums.NivelEstudio;
import mx.ine.gestiona_t.modules.cv.model.enums.StatusEstudio;
import java.time.LocalDate;
import java.util.UUID;

public record EscolaridadResponse(
    UUID id,
    NivelEstudio nivel,
    String institucion,
    String titulo,
    String cedulaProfesional,
    LocalDate fechaInicio,
    LocalDate fechaTermino,
    StatusEstudio status,
    String documentoSoportePath
) {}