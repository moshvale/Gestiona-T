package mx.ine.gestiona_t.modules.auditoria.dto.response;

import java.util.Map;

public record EstadisticasResponse(
    long totalEventosHoy,
    long totalEventosSemana,
    long totalEventosMes,
    Map<String, Long> eventosPorModulo,
    Map<String, Long> eventosPorSeveridad,
    Map<String, Long> eventosPorCategoria,
    long longitudCadena,
    String ultimoHash
) {}