package mx.ine.gestiona_t.modules.expedientes.controller;

import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearJuntaEjecutivaRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.request.CrearVocaliaRequest;
import mx.ine.gestiona_t.modules.expedientes.dto.response.JuntaEjecutivaResponse;
import mx.ine.gestiona_t.modules.expedientes.dto.response.VocaliaResponse;
import mx.ine.gestiona_t.modules.expedientes.model.enums.TipoJunta;
import mx.ine.gestiona_t.modules.expedientes.service.JuntaEjecutivaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class JuntaEjecutivaController {

    private static final Logger log = LoggerFactory.getLogger(JuntaEjecutivaController.class);

    private final JuntaEjecutivaService juntaService;

    public JuntaEjecutivaController(JuntaEjecutivaService juntaService) {
        this.juntaService = juntaService;
    }

    // ============================================
    // JUNTAS EJECUTIVAS
    // ============================================
    @PostMapping("/api/v1/juntas-ejecutivas")
    @PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<JuntaEjecutivaResponse> crearJunta(@Valid @RequestBody CrearJuntaEjecutivaRequest request) {
        log.info("🏛️ POST /api/v1/juntas-ejecutivas - Nombre: {}", request.nombre());
        return ResponseEntity.status(201).body(juntaService.crearJunta(request));
    }

    @GetMapping("/api/v1/juntas-ejecutivas")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<JuntaEjecutivaResponse>> listarJuntas(
            @RequestParam(required = false) TipoJunta tipo) {
        return ResponseEntity.ok(juntaService.listarJuntas(tipo));
    }

    @GetMapping("/api/v1/juntas-ejecutivas/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JuntaEjecutivaResponse> obtenerJunta(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(juntaService.obtenerJunta(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/api/v1/juntas-ejecutivas/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<Void> desactivarJunta(@PathVariable UUID id) {
        juntaService.desactivarJunta(id);
        return ResponseEntity.ok().build();
    }

    // ============================================
    // VOCALÍAS
    // ============================================
    @PostMapping("/api/v1/vocalias")
    @PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<VocaliaResponse> crearVocalia(@Valid @RequestBody CrearVocaliaRequest request) {
        log.info("🏛️ POST /api/v1/vocalias - Nombre: {}", request.nombre());
        return ResponseEntity.status(201).body(juntaService.crearVocalia(request));
    }

    @GetMapping("/api/v1/vocalias")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<VocaliaResponse>> listarVocaliasPorJunta(@RequestParam UUID juntaId) {
        return ResponseEntity.ok(juntaService.listarVocaliasPorJunta(juntaId));
    }
}