package mx.ine.gestiona_t.modules.expedientes.dto.response;

import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExpedienteLaboralResponse(
    UUID id,
    UUID aspiranteId,
    String nombreAspirante,
    String correoAspirante,
    String numeroEmpleado,
    TipoContratacion tipoContratacion,
    LocalDate fechaInicio,
    LocalDate fechaFin,
    Boolean vigente,
    String areaAdscripcion,
    String puestoActual,
    String nivelTabular,
    UUID vacanteId,
    String nombreVacante,
    UUID juntaEjecutivaId,
    String nombreJuntaEjecutiva,
    UUID vocaliaId,
    String nombreVocalia,
    UUID altaPorUsuarioId,
    String observaciones,
    UUID documentoSoporteId,
    String documentoSoporteNombre,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}