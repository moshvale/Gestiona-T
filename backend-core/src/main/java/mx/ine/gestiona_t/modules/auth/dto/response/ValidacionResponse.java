package mx.ine.gestiona_t.modules.auth.dto.response;

public record ValidacionResponse(
    boolean valid,
    String folio,
    String correo,
    int nivelConfianza
) {}