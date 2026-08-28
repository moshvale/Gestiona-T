package mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request;

import jakarta.validation.constraints.NotNull;

public record AceptarBloqueRequest(
    @NotNull(message = "El ID del bloque es obligatorio")
    Integer bloqueId,
    
    @NotNull(message = "La aceptacion es obligatoria")
    Boolean aceptado
) {}