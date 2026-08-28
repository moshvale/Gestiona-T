package mx.ine.gestiona_t.modules.firma.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import mx.ine.gestiona_t.modules.firma.model.enums.TipoDocumentoFirma;

public record FirmarOTPRequest(
    @NotNull(message = "El tipo de documento es obligatorio")
    TipoDocumentoFirma tipoDocumento,
    
    @NotBlank(message = "El folio del aspirante es obligatorio")
    String folioAspirante,
    
    @NotBlank(message = "El nombre del archivo es obligatorio")
    String nombreArchivo,
    
    @NotBlank(message = "El contenido del documento es obligatorio")
    String contenidoDocumentoBase64,
    
    @NotBlank(message = "El OTP de email es obligatorio")
    String otpEmail,
    
    @NotBlank(message = "El OTP de SMS es obligatorio")
    String otpSms
) {}