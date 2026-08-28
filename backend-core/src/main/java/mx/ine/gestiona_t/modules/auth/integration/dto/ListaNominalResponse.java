package mx.ine.gestiona_t.modules.auth.integration.dto;

public record ListaNominalResponse(
    boolean vigente,
    String nombreCompleto,
    String curp,
    String claveElector,
    String seccion,
    String vigencia,
    String mensaje
) {}