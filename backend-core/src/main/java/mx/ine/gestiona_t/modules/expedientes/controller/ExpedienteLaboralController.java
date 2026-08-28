package mx.ine.gestiona_t.modules.expedientes.controller;

import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auth.dto.response.MensajeResponse;
import mx.ine.gestiona_t.modules.expedientes.dto.request.ActualizarExpedienteLaboralRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearExpedienteLaboralRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.response.ExpedienteLaboralResponse;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoContratacion;
import mx.ine.gestiona_t.modules.expedientes.service.ExpedienteLaboralService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expedientes-laborales")
public class ExpedienteLaboralController {

    private static final Logger log = LoggerFactory.getLogger(ExpedienteLaboralController.class);

    private final ExpedienteLaboralService expedienteService;

    public ExpedienteLaboralController(ExpedienteLaboralService expedienteService) {
        this.expedienteService = expedienteService;
    }

    // ============================================
    // 1. CREAR EXPEDIENTE LABORAL
    // ============================================
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<?> crear(@Valid @RequestBody CrearExpedienteLaboralRequest request) {
        UUID usuarioId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        log.info("📝 POST /api/v1/expedientes-laborales - Usuario: {}", usuarioId);
        try {
            return ResponseEntity.status(201).body(expedienteService.crear(request, usuarioId));
        } catch (RuntimeException e) {
            log.error("❌ Error al crear expediente: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============================================
    // 2. LISTAR TODOS (con filtros opcionales)
    // ============================================
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<List<ExpedienteLaboralResponse>> listar(
            @RequestParam(required = false) Boolean soloVigentes,
            @RequestParam(required = false) TipoContratacion tipo) {
        log.info("📂 GET /api/v1/expedientes-laborales - SoloVigentes: {} | Tipo: {}", soloVigentes, tipo);
        return ResponseEntity.ok(expedienteService.listarTodos(soloVigentes, tipo));
    }

    // ============================================
    // 3. OBTENER POR ID
    // ============================================
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<ExpedienteLaboralResponse> obtenerPorId(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(expedienteService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ============================================
    // 4. OBTENER VIGENTE POR ASPIRANTE
    // ============================================
    @GetMapping("/aspirante/{aspiranteId}/vigente")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<ExpedienteLaboralResponse> obtenerVigentePorAspirante(@PathVariable UUID aspiranteId) {
        ExpedienteLaboralResponse response = expedienteService.obtenerVigentePorAspirante(aspiranteId);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // ============================================
    // 5. OBTENER VIGENTE POR NÚMERO DE EMPLEADO
    // ============================================
    @GetMapping("/empleado/{numeroEmpleado}/vigente")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<ExpedienteLaboralResponse> obtenerVigentePorNumeroEmpleado(@PathVariable String numeroEmpleado) {
        ExpedienteLaboralResponse response = expedienteService.obtenerVigentePorNumeroEmpleado(numeroEmpleado);
        return response != null ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }

    // ============================================
    // 6. HISTORIAL COMPLETO DE UN ASPIRANTE
    // ============================================
    @GetMapping("/aspirante/{aspiranteId}/historial")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<List<ExpedienteLaboralResponse>> obtenerHistorial(@PathVariable UUID aspiranteId) {
        return ResponseEntity.ok(expedienteService.obtenerHistorialPorAspirante(aspiranteId));
    }

    // ============================================
    // 7. LISTAR POR JUNTA EJECUTIVA
    // ============================================
    @GetMapping("/junta/{juntaId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<List<ExpedienteLaboralResponse>> listarPorJunta(@PathVariable UUID juntaId) {
        return ResponseEntity.ok(expedienteService.listarPorJuntaEjecutiva(juntaId));
    }

    // ============================================
    // 8. ACTUALIZAR EXPEDIENTE
    // ============================================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_PRINCIPAL', 'ROLE_RESPONSABLE_JLE', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<?> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarExpedienteLaboralRequest request) {
        log.info("✏️ PUT /api/v1/expedientes-laborales/{}", id);
        try {
            return ResponseEntity.ok(expedienteService.actualizar(id, request));
        } catch (RuntimeException e) {
            log.error("❌ Error al actualizar: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ============================================
    // 9. CERRAR EXPEDIENTE (baja lógica)
    // ============================================
    @PostMapping("/{id}/cerrar")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_PRINCIPAL', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<ExpedienteLaboralResponse> cerrar(@PathVariable UUID id) {
        UUID usuarioId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        log.info("🔒 POST /api/v1/expedientes-laborales/{}/cerrar - Usuario: {}", id, usuarioId);
        try {
            return ResponseEntity.ok(expedienteService.cerrar(id, usuarioId));
        } catch (RuntimeException e) {
            log.error("❌ Error al cerrar: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================
    // 10. ELIMINAR EXPEDIENTE (borrado físico, solo admin)
    // ============================================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<MensajeResponse> eliminar(@PathVariable UUID id) {
        log.info("🗑️ DELETE /api/v1/expedientes-laborales/{}", id);
        try {
            expedienteService.eliminar(id);
            return ResponseEntity.ok(new MensajeResponse("Expediente eliminado correctamente", 200));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}