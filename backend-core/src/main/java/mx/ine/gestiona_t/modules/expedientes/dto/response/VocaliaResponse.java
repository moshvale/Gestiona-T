package mx.ine.gestiona_t.modules.expedientes.dto.response;

import java.util.UUID;

public record VocaliaResponse(
    UUID id,
    String nombre,
    UUID juntaEjecutivaId,
    String nombreJuntaEjecutiva,
    Boolean activa
) {}