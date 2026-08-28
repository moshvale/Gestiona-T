package mx.ine.gestiona_t.modules.cv.integration.dto;

import java.util.List;
import java.util.Map;

public record MapeoResponse(
    Map<String, Object> datosMapeados,
    List<String> camposFaltantes,
    double porcentajeExito,
    String mensaje
) {}