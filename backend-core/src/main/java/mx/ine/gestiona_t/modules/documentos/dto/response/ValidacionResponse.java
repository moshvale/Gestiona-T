package mx.ine.gestiona_t.modules.documentos.dto.response;

import java.util.UUID;

public record ValidacionResponse(
    UUID documentoId,
    boolean exitoso,
    String mensaje,
    Double scoreAutenticidad,
    String metodoValidacion
) {}