package mx.ine.gestiona_t.modules.expedientes.dto.request;

import jakarta.validation.constraints.*;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;

import java.time.LocalDate;
import java.util.UUID;

public record CrearExpedienteLaboralRequest(

    @NotNull(message = "El ID del aspirante es obligatorio")
    UUID aspiranteId,

    @NotBlank(message = "El número de empleado es obligatorio")
    @Pattern(regexp = "^[0-9]{1,20}$", message = "El número de empleado solo puede contener dígitos (máximo 20)")
    String numeroEmpleado,

    @NotNull(message = "El tipo de contratación es obligatorio")
    TipoContratacion tipoContratacion,

    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio,

    LocalDate fechaFin,

    @Size(max = 200, message = "El área de adscripción no puede exceder 200 caracteres")
    String areaAdscripcion,

    @Size(max = 200, message = "El puesto actual no puede exceder 200 caracteres")
    String puestoActual,

    @Size(max = 20, message = "El nivel tabular no puede exceder 20 caracteres")
    String nivelTabular,

    UUID vacanteId,

    UUID juntaEjecutivaId,

    UUID vocaliaId,

    @Size(max = 2000, message = "Las observaciones no pueden exceder 2000 caracteres")
    String observaciones
) {}