package mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response;

import java.time.LocalDateTime;

public record AceptacionResponse(
    Long id,
    Integer bloqueId,
    boolean aceptado,
    LocalDateTime timestampAceptacion,
    String hashTextoBloque
) {}