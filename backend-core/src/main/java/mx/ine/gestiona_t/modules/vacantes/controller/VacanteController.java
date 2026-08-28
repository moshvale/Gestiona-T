package mx.ine.gestiona_t.modules.vacantes.controller;

import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auth.dto.response.MensajeResponse;
import mx.ine.gestiona_t.modules.vacantes.dto.request.CrearVacanteRequest;
import mx.ine.gestiona_t.modules.vacantes.dto.response.VacanteResumenResponse;
import mx.ine.gestiona_t.modules.vacantes.dto.response.VacanteResponse;
import mx.ine.gestiona_t.modules.vacantes.service.VacanteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vacantes")
public class VacanteController {

    private static final Logger log = LoggerFactory.getLogger(VacanteController.class);
    private final VacanteService vacanteService;

    public VacanteController(VacanteService vacanteService) {
        this.vacanteService = vacanteService;
    }

    /**
     * ✅ Crea una nueva vacante. Solo analistas.
     */
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<VacanteResponse> crearVacante(@Valid @RequestBody CrearVacanteRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID analistaId = UUID.fromString(auth.getName());
        log.info("📋 POST /api/v1/vacantes - Analista: {} - Puesto: {}", analistaId, request.puesto());
        
        try {
            VacanteResponse response = vacanteService.crearVacante(request, analistaId);
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Lista todas las vacantes activas (accesible para analistas y aspirantes).
     */
    @GetMapping
    public ResponseEntity<List<VacanteResumenResponse>> listarVacantes(
            @RequestParam(required = false) String busqueda) {
        log.info("📂 GET /api/v1/vacantes - Búsqueda: '{}'", busqueda != null ? busqueda : "ninguna");
        
        List<VacanteResumenResponse> vacantes;
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            vacantes = vacanteService.buscarVacantes(busqueda);
        } else {
            vacantes = vacanteService.listarVacantes();
        }
        return ResponseEntity.ok(vacantes);
    }

    /**
     * Obtiene el detalle completo de una vacante.
     */
    @GetMapping("/{id}")
    public ResponseEntity<VacanteResponse> obtenerVacante(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(vacanteService.obtenerVacante(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualiza una vacante existente. Solo analistas.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<VacanteResponse> actualizarVacante(
            @PathVariable UUID id,
            @Valid @RequestBody CrearVacanteRequest request) {
        log.info("✏️ PUT /api/v1/vacantes/{}", id);
        try {
            return ResponseEntity.ok(vacanteService.actualizarVacante(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Desactiva una vacante (soft delete). Solo analistas.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<MensajeResponse> desactivarVacante(@PathVariable UUID id) {
        log.info("🚫 DELETE /api/v1/vacantes/{}", id);
        try {
            vacanteService.desactivarVacante(id);
            return ResponseEntity.ok(new MensajeResponse("Vacante desactivada exitosamente", 200));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}