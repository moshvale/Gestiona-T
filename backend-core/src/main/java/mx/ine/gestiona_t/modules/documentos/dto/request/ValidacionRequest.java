package mx.ine.gestiona_t.modules.documentos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidacionRequest(
    @NotBlank(message = "El estatus es obligatorio")
    String estatus, // "VALIDADO" o "RECHAZADO"
    
    String motivo   // Obligatorio si el estatus es RECHAZADO
) {}