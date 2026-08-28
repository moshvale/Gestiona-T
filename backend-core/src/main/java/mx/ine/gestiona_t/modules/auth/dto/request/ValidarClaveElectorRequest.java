package mx.ine.gestiona_t.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ValidarClaveElectorRequest(
    @NotBlank(message = "La clave de elector es obligatoria")
    @Pattern(regexp = "^[A-Z]{6}[0-9]{8}[HM][0-9]{3}$", 
             message = "El formato de la clave de elector no es válido")
    String claveElector,
    
    @NotBlank(message = "La imagen frontal de la credencial es obligatoria")
    String ocrCredencialFrontal,
    
    @NotBlank(message = "La imagen posterior de la credencial es obligatoria")
    String ocrCredencialPosterior
) {}