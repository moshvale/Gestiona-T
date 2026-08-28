package mx.ine.gestiona_t.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CrearAnalistaRequest(
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    String nombreCompleto,

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    String correoElectronico,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 12, max = 100, message = "La contraseña debe tener entre 12 y 100 caracteres")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).+$",
        message = "La contraseña debe contener al menos una mayúscula, un número y un carácter especial"
    )
    String password,

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
        regexp = "^(ANALISTA_UR|ADMIN_SISTEMA|CONTRALORIA)$",
        message = "El rol debe ser ANALISTA_UR, ADMIN_SISTEMA o CONTRALORIA"
    )
    String rol
) {}