package mx.ine.gestiona_t.modules.matching.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ResultadoMatchingResponse(
    UUID id,
    UUID aspiranteId,
    String folio,
    int scoreCompatibilidad,
    String resumenEjecutivo,
    List<String> fortalezas,
    List<String> areasMejora,
    String requisitosVacante,
    String modeloIa,
    LocalDateTime fechaEvaluacion
) {}