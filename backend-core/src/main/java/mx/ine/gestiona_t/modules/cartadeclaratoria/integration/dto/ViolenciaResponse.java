package mx.ine.gestiona_t.modules.cartadeclaratoria.integration.dto;

public record ViolenciaResponse(
    boolean conAntecedentes,
    String curp,
    String nombreCompleto,
    String tipoViolencia,
    String autoridad,
    String fechaRegistro,
    String mensaje
) {}