package mx.ine.gestiona_t.modules.cv.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.ine.gestiona_t.modules.cv.model.enums.NivelMando;
import java.time.LocalDate;

public record ExperienciaRequest(
    @NotBlank(message = "La institución es obligatoria")
    @Size(max = 200, message = "La institución no puede exceder 200 caracteres")
    String institucion,
    
    @Size(max = 13, message = "El RFC no puede exceder 13 caracteres")
    String rfcInstitucion,
    
    @NotBlank(message = "El puesto es obligatorio")
    @Size(max = 100, message = "El puesto no puede exceder 100 caracteres")
    String puesto,
    
    @NotBlank(message = "Las funciones son obligatorias")
    @Size(max = 1000, message = "Las funciones no pueden exceder 1000 caracteres")
    String funciones,
    
    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio,
    
    LocalDate fechaTermino,
    
    @NotNull(message = "Debe indicar si actualmente labora ahí")
    boolean actualmenteLaborando,
    
    @NotNull(message = "El nivel de mando es obligatorio")
    NivelMando nivelMando
) {}