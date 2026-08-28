package mx.ine.gestiona_t.modules.matching.dto;

import java.util.List;

public record MatchingRequest(
    String aspiranteId,
    String perfilProfesionalResumido, // Texto consolidado de experiencia y formación
    List<String> habilidades,
    List<String> idiomas,
    String requisitosVacante // Opcional: para comparar contra una convocatoria específica
) {}