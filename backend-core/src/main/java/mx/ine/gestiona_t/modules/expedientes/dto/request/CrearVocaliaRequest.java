package mx.ine.gestiona_t.modules.expedientes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CrearVocaliaRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    String nombre,

    @NotNull(message = "El ID de la junta ejecutiva es obligatorio")
    UUID juntaEjecutivaId
) {}