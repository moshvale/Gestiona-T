package mx.ine.gestiona_t.modules.auth.integration.dto;

public record RenapoResponse(
    boolean valido,
    String nombreCompleto,
    String curp,
    String rfc,
    String fechaNacimiento,
    String entidadFederativa,
    String sexo,
    String nacionalidad,
    String mensaje
) {}