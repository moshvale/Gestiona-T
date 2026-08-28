package mx.ine.gestiona_t.modules.expedientes.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoJunta;

public record CrearJuntaEjecutivaRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 200)
    String nombre,

    @NotNull(message = "El tipo de junta es obligatorio")
    TipoJunta tipo,

    @Size(max = 100)
    String estado,

    @Size(max = 20)
    String claveIne
) {}