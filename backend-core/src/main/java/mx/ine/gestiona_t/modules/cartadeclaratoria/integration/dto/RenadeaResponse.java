package mx.ine.gestiona_t.modules.cartadeclaratoria.integration.dto;

public record RenadeaResponse(
    boolean registrado,
    String curp,
    String nombreCompleto,
    String motivo,
    String autoridadRegistro,
    String fechaRegistro,
    String mensaje
) {}