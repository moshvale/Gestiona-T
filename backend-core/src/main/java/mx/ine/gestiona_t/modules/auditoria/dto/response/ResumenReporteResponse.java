package mx.ine.gestiona_t.modules.auditoria.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

public record ResumenReporteResponse(
    LocalDateTime fechaGeneracion,
    LocalDateTime rangoDesde,
    LocalDateTime rangoHasta,
    long totalEventos,
    Map<String, Long> eventosPorCategoria,
    Map<String, Long> eventosPorSeveridad,
    Map<String, Long> eventosPorModulo,
    long eventosCriticos,
    long eventosError,
    boolean integridadCadenaValida
) {}