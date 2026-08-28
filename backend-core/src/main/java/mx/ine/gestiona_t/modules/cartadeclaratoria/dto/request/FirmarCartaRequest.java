package mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.MetodoFirmaCarta;

public record FirmarCartaRequest(
    @NotNull(message = "El metodo de firma es obligatorio")
    MetodoFirmaCarta metodoFirma,
    
    String firmaDigital,
    
    String otp,
    
    String biometriaHash
) {}