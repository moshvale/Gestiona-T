package mx.ine.gestiona_t.modules.expedientes.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;

import java.time.LocalDate;
import java.util.UUID;

public record ActualizarExpedienteLaboralRequest(

    @Pattern(regexp = "^[0-9]{1,20}$", message = "El número de empleado solo puede contener dígitos (máximo 20)")
    String numeroEmpleado,

    TipoContratacion tipoContratacion,

    LocalDate fechaInicio,

    LocalDate fechaFin,

    @Size(max = 200)
    String areaAdscripcion,

    @Size(max = 200)
    String puestoActual,

    @Size(max = 20)
    String nivelTabular,

    UUID vacanteId,

    UUID juntaEjecutivaId,

    UUID vocaliaId,

    @Size(max = 2000)
    String observaciones
) {}