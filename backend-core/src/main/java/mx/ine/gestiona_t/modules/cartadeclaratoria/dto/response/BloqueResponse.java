package mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response;

public record BloqueResponse(
    Integer id,
    String titulo,
    String texto,
    String fundamentoLegal,
    boolean obligatorio,
    Integer orden,
    boolean aceptado
) {}