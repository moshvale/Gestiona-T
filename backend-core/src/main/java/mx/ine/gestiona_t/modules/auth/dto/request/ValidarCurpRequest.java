package mx.ine.gestiona_t.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public record ValidarCurpRequest(
    @NotBlank(message = "La CURP es obligatoria")
    @Pattern(regexp = "^[A-Z]{4}[0-9]{6}[HM][A-Z]{5}[A-Z0-9][0-9]$", 
             message = "El formato de la CURP no es válido")
    String curp,
    
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,
    
    @NotBlank(message = "El apellido paterno es obligatorio")
    String apellidoPaterno,
    
    String apellidoMaterno,
    
    @Past(message = "La fecha de nacimiento debe ser pasada")
    LocalDate fechaNacimiento,
    
    @Pattern(regexp = "^[A-Z]{2}$", message = "La entidad federativa debe tener 2 letras")
    String entidadFederativa
) {}