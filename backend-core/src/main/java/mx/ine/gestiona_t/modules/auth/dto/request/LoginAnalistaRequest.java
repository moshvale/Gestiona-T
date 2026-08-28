package mx.ine.gestiona_t.modules.auth.dto.request;

public record LoginAnalistaRequest(
    String correo,
    String password
) {}