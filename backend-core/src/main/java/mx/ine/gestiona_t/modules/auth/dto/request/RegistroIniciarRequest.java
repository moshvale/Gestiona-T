package mx.ine.gestiona_t.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroIniciarRequest(
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El formato del correo no es válido")
    String correo,

    @NotBlank(message = "El teléfono es obligatorio")
    String telefono,

    String curp,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    String password
) {}