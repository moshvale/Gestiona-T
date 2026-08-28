package mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response;

import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.TipoValidacionExterna;
import java.time.LocalDateTime;

public record ValidacionExternaResponse(
    Long id,
    TipoValidacionExterna tipoValidacion,
    boolean resultado,
    String mensaje,
    LocalDateTime fechaConsulta
) {}