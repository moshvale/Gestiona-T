package mx.ine.gestiona_t.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ReenviarOtpRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    String correo
) {}