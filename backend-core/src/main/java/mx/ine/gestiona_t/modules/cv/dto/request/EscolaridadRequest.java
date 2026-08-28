package mx.ine.gestiona_t.modules.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.ine.gestiona_t.modules.cv.model.enums.NivelEstudio;
import mx.ine.gestiona_t.modules.cv.model.enums.StatusEstudio;
import java.time.LocalDate;

public record EscolaridadRequest(
    @NotNull(message = "El nivel de estudio es obligatorio")
    NivelEstudio nivel,
    
    @NotBlank(message = "La institución es obligatoria")
    @Size(max = 200, message = "La institución no puede exceder 200 caracteres")
    String institucion,
    
    @Size(max = 100, message = "El título no puede exceder 100 caracteres")
    String titulo,
    
    @Size(max = 20, message = "La cédula profesional no puede exceder 20 caracteres")
    String cedulaProfesional,
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio,
    
    LocalDate fechaTermino,
    
    @NotNull(message = "El status es obligatorio")
    StatusEstudio status
) {}