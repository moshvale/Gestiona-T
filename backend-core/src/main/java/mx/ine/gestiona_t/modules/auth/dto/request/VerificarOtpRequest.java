package mx.ine.gestiona_t.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificarOtpRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    String correo,
    
    @NotBlank(message = "El OTP es obligatorio")
    @Pattern(regexp = "^[0-9]{6}$", message = "El OTP debe tener 6 dígitos")
    String otp
) {}