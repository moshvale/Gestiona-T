package mx.ine.gestiona_t.modules.documentos.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusRevision;

public record DictamenRevisionRequest(
    @NotNull(message = "El estatus es obligatorio")
    EstatusRevision estatus,
    
    @NotBlank(message = "El dictamen es obligatorio")
    String dictamen,
    
    String motivo
) {}