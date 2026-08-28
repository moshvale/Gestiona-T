package mx.ine.gestiona_t.modules.postulaciones.dto.response;

import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusFinalSeleccion;
import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusPostulacion;

import java.time.LocalDateTime;
import java.util.UUID;

public record PostulacionResponse(
    UUID id,
    UUID aspiranteId,
    String nombreAspirante,
    UUID vacanteId,
    String nombrePuesto,
    EstatusPostulacion estatus,
    LocalDateTime fechaPostulacion,
    String observaciones,
    UUID cartaDeclaratoriaId,
    Boolean cvCompletado,
    Boolean documentosCompletos,
    // ✅ NUEVOS CAMPOS
    Double calificacionConocimientos,
    Double calificacionPsicometrica,
    Double calificacionEntrevista,
    EstatusFinalSeleccion estatusFinalSeleccion,
    String dictamenFinal
) {}