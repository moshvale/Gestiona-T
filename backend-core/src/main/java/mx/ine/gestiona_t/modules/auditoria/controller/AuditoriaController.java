package mx.ine.gestiona_t.modules.auditoria.controller;

import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auditoria.dto.request.BuscarEventosRequest;
import mx.ine.gestiona_t.modules.auditoria.dto.response.*;
import mx.ine.gestiona_t.modules.auditoria.service.AuditoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auditoria")
public class AuditoriaController {
    
    private static final Logger log = LoggerFactory.getLogger(AuditoriaController.class);
    private final AuditoriaService auditoriaService;
    
    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }
    
    @PostMapping("/eventos/buscar")
    public ResponseEntity<Page<EventoAuditoriaResponse>> buscarEventos(
            @Valid @RequestBody BuscarEventosRequest request) {
        log.info("POST /api/v1/auditoria/eventos/buscar");
        return ResponseEntity.ok(auditoriaService.buscarEventos(request));
    }
    
    @GetMapping("/eventos/{id}")
    public ResponseEntity<EventoAuditoriaResponse> obtenerEvento(@PathVariable UUID id) {
        log.info("GET /api/v1/auditoria/eventos/{}", id);
        return ResponseEntity.ok(auditoriaService.obtenerEvento(id));
    }
    
    @GetMapping("/eventos/aspirante/{aspiranteId}")
    public ResponseEntity<Page<EventoAuditoriaResponse>> obtenerEventosActor(
            @PathVariable UUID aspiranteId,
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "100") int tamano) {
        log.info("GET /api/v1/auditoria/eventos/aspirante/{}", aspiranteId);
        return ResponseEntity.ok(auditoriaService.obtenerEventosActor(aspiranteId, pagina, tamano));
    }
    
    @GetMapping("/reportes/resumen")
    public ResponseEntity<ResumenReporteResponse> generarResumen(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        log.info("GET /api/v1/auditoria/reportes/resumen");
        return ResponseEntity.ok(auditoriaService.generarResumen(desde, hasta));
    }
    
    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasResponse> obtenerEstadisticas() {
        log.info("GET /api/v1/auditoria/estadisticas");
        return ResponseEntity.ok(auditoriaService.obtenerEstadisticas());
    }
    
    @GetMapping("/eventos/verificar-cadena")
    public ResponseEntity<VerificacionIntegridadResponse> verificarCadena() {
        log.info("GET /api/v1/auditoria/eventos/verificar-cadena");
        return ResponseEntity.ok(auditoriaService.verificarIntegridadCadena());
    }
    
    @GetMapping(value = "/reportes/exportar-csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        log.info("GET /api/v1/auditoria/reportes/exportar-csv");
        
        byte[] csv = auditoriaService.exportarExcel(desde, hasta);
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=auditoria-" + System.currentTimeMillis() + ".csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
    }
}