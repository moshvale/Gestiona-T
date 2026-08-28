package mx.ine.gestiona_t.modules.cv.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CursoRequest(
    @NotBlank(message = "El nombre del curso es obligatorio")
    @Size(max = 200, message = "El nombre no puede exceder 200 caracteres")
    String nombreCurso,
    
    @NotBlank(message = "La institución es obligatoria")
    @Size(max = 200, message = "La institución no puede exceder 200 caracteres")
    String institucion,
    
    @NotNull(message = "La duración es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1 hora")
    Integer duracionHoras,
    
    @NotNull(message = "La fecha de realización es obligatoria")
    LocalDate fechaRealizacion
) {}