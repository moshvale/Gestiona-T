package mx.ine.gestiona_t.modules.expedientes.dto.response;

import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoJunta;

import java.util.UUID;

public record JuntaEjecutivaResponse(
    UUID id,
    String nombre,
    TipoJunta tipo,
    String estado,
    String claveIne,
    Boolean activa
) {}