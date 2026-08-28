package mx.ine.gestiona_t.modules.auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalistaResumenDTO(
    UUID id,
    String nombreCompleto,
    String correoElectronico,
    String rol,
    boolean activo,
    LocalDateTime createdAt
) {}