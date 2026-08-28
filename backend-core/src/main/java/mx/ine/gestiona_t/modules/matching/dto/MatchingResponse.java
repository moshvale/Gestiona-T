package mx.ine.gestiona_t.modules.matching.dto;

import java.util.List;

public record MatchingResponse(
    int scoreCompatibilidad,       // 0 a 100
    String resumenEjecutivo,       // Justificación breve del score
    List<String> fortalezas,       // Puntos fuertes detectados
    List<String> areasMejora       // Brechas o áreas de oportunidad
) {}