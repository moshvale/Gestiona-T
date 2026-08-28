package mx.ine.gestiona_t.modules.cv.dto.response;

import mx.ine.gestiona_t.modules.cv.model.enums.NivelMando;
import java.time.LocalDate;
import java.util.UUID;

public record ExperienciaResponse(
    UUID id,
    String institucion,
    String rfcInstitucion,
    String puesto,
    String funciones,
    LocalDate fechaInicio,
    LocalDate fechaTermino,
    boolean actualmenteLaborando,
    NivelMando nivelMando,
    String documentoSoportePath
) {}