package mx.ine.gestiona_t.modules.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.ine.gestiona_t.modules.cv.model.enums.NivelHabilidad;
import mx.ine.gestiona_t.modules.cv.model.enums.TipoHabilidad;
import java.time.LocalDate;

public record HabilidadRequest(
    @NotNull(message = "El tipo de habilidad es obligatorio")
    TipoHabilidad tipo,
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    String nombre,
    
    @NotNull(message = "El nivel es obligatorio")
    NivelHabilidad nivel,
    
    LocalDate fechaCertificacion,
    
    LocalDate fechaVencimiento
) {}