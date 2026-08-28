package mx.ine.gestiona_t.modules.postulaciones.controller;

import mx.ine.gestiona_t.modules.auth.dto.response.MensajeResponse;
import mx.ine.gestiona_t.modules.postulaciones.dto.response.PostulacionResponse;
import mx.ine.gestiona_t.modules.postulaciones.model.Postulacion;
import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusFinalSeleccion; // ✅ NUEVO IMPORT
import mx.ine.gestiona_t.modules.postulaciones.model.enums.EstatusPostulacion;
import mx.ine.gestiona_t.modules.postulaciones.repository.PostulacionRepository;
import mx.ine.gestiona_t.modules.postulaciones.service.PostulacionService;
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
@RequestMapping("/api/v1/postulaciones")
public class PostulacionController {

    private static final Logger log = LoggerFactory.getLogger(PostulacionController.class);
    
    private final PostulacionService postulacionService;
    private final PostulacionRepository postulacionRepository;

    public PostulacionController(PostulacionService postulacionService, 
                                 PostulacionRepository postulacionRepository) {
        this.postulacionService = postulacionService;
        this.postulacionRepository = postulacionRepository;
    }

    // ============================================
    // 1. LISTAR TODAS LAS POSTULACIONES (Analista/Admin)
    // ============================================
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<List<PostulacionResponse>> obtenerTodasLasPostulaciones() {
        log.info("📂 GET /api/v1/postulaciones - Consulta global de analista");
        return ResponseEntity.ok(postulacionService.obtenerTodasLasPostulaciones());
    }

    // ============================================
    // 2. POSTULARSE A UNA VACANTE (Aspirante)
    // ============================================
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ASPIRANTE')")
    public ResponseEntity<PostulacionResponse> postularse(@RequestParam UUID vacanteId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID aspiranteId = UUID.fromString(auth.getName());
        log.info("📝 POST /api/v1/postulaciones - Aspirante: {} a Vacante: {}", aspiranteId, vacanteId);
        try {
            return ResponseEntity.status(201).body(postulacionService.postularse(aspiranteId, vacanteId));
        } catch (RuntimeException e) {
            log.error("❌ Error al postularse: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================
    // 3. OBTENER MIS POSTULACIONES (Aspirante)
    // ============================================
    @GetMapping("/mis-postulaciones")
    @PreAuthorize("hasAuthority('ROLE_ASPIRANTE')")
    public ResponseEntity<List<PostulacionResponse>> obtenerMisPostulaciones() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID aspiranteId = UUID.fromString(auth.getName());
        log.info("📂 GET /api/v1/postulaciones/mis-postulaciones - Aspirante: {}", aspiranteId);
        return ResponseEntity.ok(postulacionService.obtenerMisPostulaciones(aspiranteId));
    }

    // ============================================
    // 4. OBTENER POSTULACIONES POR VACANTE (Analista/Admin)
    // ============================================
    @GetMapping("/vacante/{vacanteId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<List<PostulacionResponse>> obtenerPostulacionesPorVacante(@PathVariable UUID vacanteId) {
        log.info("📂 GET /api/v1/postulaciones/vacante/{} - Consulta de analista", vacanteId);
        return ResponseEntity.ok(postulacionService.obtenerPostulacionesPorVacante(vacanteId));
    }

    // ============================================
    // 5. ACTUALIZAR ESTATUS DE POSTULACIÓN (Analista/Admin)
    // ============================================
    @PutMapping("/{id}/estatus")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<PostulacionResponse> actualizarEstatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> request) {
        log.info("✏️ PUT /api/v1/postulaciones/{}/estatus", id);
        try {
            EstatusPostulacion estatus = EstatusPostulacion.valueOf(request.get("estatus").toUpperCase());
            String observaciones = request.getOrDefault("observaciones", "");
            return ResponseEntity.ok(postulacionService.actualizarEstatus(id, estatus, observaciones));
        } catch (RuntimeException e) {
            log.error("❌ Error al actualizar estatus: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================
    // ✅ 6. NUEVO: ACTUALIZAR EVALUACIÓN Y DICTAMEN (Analista/Admin)
    // ============================================
    @PutMapping("/{id}/evaluacion")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<PostulacionResponse> actualizarEvaluacion(
            @PathVariable UUID id,
            @RequestBody Map<String, Object> request) {
        
        log.info("✏️ PUT /api/v1/postulaciones/{}/evaluacion", id);
        
        try {
            Postulacion postulacion = postulacionRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));

            // Actualizar calificaciones (manejando nulls si no aplican)
            if (request.containsKey("calificacionConocimientos")) {
                Object val = request.get("calificacionConocimientos");
                postulacion.setCalificacionConocimientos(val != null ? Double.valueOf(val.toString()) : null);
            }
            
            if (request.containsKey("calificacionPsicometrica")) {
                Object val = request.get("calificacionPsicometrica");
                postulacion.setCalificacionPsicometrica(val != null ? Double.valueOf(val.toString()) : null);
            }
            
            if (request.containsKey("calificacionEntrevista")) {
                Object val = request.get("calificacionEntrevista");
                postulacion.setCalificacionEntrevista(val != null ? Double.valueOf(val.toString()) : null);
            }

            if (request.containsKey("estatusFinalSeleccion")) {
                EstatusFinalSeleccion dictamen = EstatusFinalSeleccion.valueOf(request.get("estatusFinalSeleccion").toString());
                postulacion.setEstatusFinalSeleccion(dictamen);
                if (dictamen == EstatusFinalSeleccion.SELECCIONADO) {
                    postulacion.setEstatus(EstatusPostulacion.ACEPTADA);
                } else if (dictamen == EstatusFinalSeleccion.NO_SELECCIONADO) {
                    postulacion.setEstatus(EstatusPostulacion.RECHAZADA);
                } else {
                    postulacion.setEstatus(EstatusPostulacion.EN_REVISION);
                }
            }

            if (request.containsKey("dictamenFinal")) {
                postulacion.setDictamenFinal(request.get("dictamenFinal").toString());
            }

            postulacion = postulacionRepository.save(postulacion);
            log.info("✅ Evaluación actualizada para postulación {}", id);
            
            return ResponseEntity.ok(mapToResponse(postulacion));
        } catch (RuntimeException e) {
            log.error("❌ Error al actualizar evaluación: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================
    // 7. VINCULAR CARTA DECLARATORIA (Aspirante)
    // ============================================
    @PostMapping("/{postulacionId}/carta-declaratoria/{cartaId}")
    @PreAuthorize("hasAuthority('ROLE_ASPIRANTE')")
    public ResponseEntity<PostulacionResponse> vincularCartaDeclaratoria(
            @PathVariable UUID postulacionId,
            @PathVariable UUID cartaId) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID aspiranteId = UUID.fromString(auth.getName());
        log.info("📜 POST /api/v1/postulaciones/{}/carta-declaratoria/{} - Aspirante: {}", 
                 postulacionId, cartaId, aspiranteId);
        try {
            Postulacion postulacion = postulacionRepository.findById(postulacionId)
                    .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
            
            if (!postulacion.getAspiranteId().equals(aspiranteId)) {
                log.warn("⚠️ Intento de vincular carta en postulación ajena. Aspirante: {}, Postulación: {}", 
                         aspiranteId, postulacionId);
                return ResponseEntity.status(403).build();
            }
            
            postulacion.setCartaDeclaratoriaId(cartaId);
            postulacion = postulacionRepository.save(postulacion);
            
            log.info("✅ Carta declaratoria {} vinculada a postulación {}", cartaId, postulacionId);
            return ResponseEntity.ok(mapToResponse(postulacion));
        } catch (RuntimeException e) {
            log.error("❌ Error al vincular carta: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================
    // 8. MARCAR CV COMO COMPLETADO (Aspirante)
    // ============================================
    @PostMapping("/{postulacionId}/cv-completado")
    @PreAuthorize("hasAuthority('ROLE_ASPIRANTE')")
    public ResponseEntity<PostulacionResponse> marcarCvCompletado(@PathVariable UUID postulacionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID aspiranteId = UUID.fromString(auth.getName());
        log.info("📄 POST /api/v1/postulaciones/{}/cv-completado - Aspirante: {}", postulacionId, aspiranteId);
        try {
            Postulacion postulacion = postulacionRepository.findById(postulacionId)
                    .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
            if (!postulacion.getAspiranteId().equals(aspiranteId)) {
                return ResponseEntity.status(403).build();
            }
            postulacion.setCvCompletado(true);
            postulacion = postulacionRepository.save(postulacion);
            log.info("✅ CV marcado como completado en postulación {}", postulacionId);
            return ResponseEntity.ok(mapToResponse(postulacion));
        } catch (RuntimeException e) {
            log.error("❌ Error al marcar CV: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================
    // 9. MARCAR DOCUMENTOS COMO COMPLETOS (Aspirante)
    // ============================================
    @PostMapping("/{postulacionId}/documentos-completos")
    @PreAuthorize("hasAuthority('ROLE_ASPIRANTE')")
    public ResponseEntity<PostulacionResponse> marcarDocumentosCompletos(@PathVariable UUID postulacionId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UUID aspiranteId = UUID.fromString(auth.getName());
        log.info("📎 POST /api/v1/postulaciones/{}/documentos-completos - Aspirante: {}", postulacionId, aspiranteId);
        try {
            Postulacion postulacion = postulacionRepository.findById(postulacionId)
                    .orElseThrow(() -> new RuntimeException("Postulación no encontrada"));
            if (!postulacion.getAspiranteId().equals(aspiranteId)) {
                return ResponseEntity.status(403).build();
            }
            postulacion.setDocumentosCompletos(true);
            postulacion = postulacionRepository.save(postulacion);
            log.info("✅ Documentos marcados como completos en postulación {}", postulacionId);
            return ResponseEntity.ok(mapToResponse(postulacion));
        } catch (RuntimeException e) {
            log.error("❌ Error al marcar documentos: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // ============================================
    // MÉTODO HELPER: Mapear entidad a DTO
    // ============================================
    private PostulacionResponse mapToResponse(Postulacion p) {
        return postulacionService.obtenerMisPostulaciones(p.getAspiranteId()).stream()
                .filter(resp -> resp.id().equals(p.getId()))
                .findFirst()
                .orElse(new PostulacionResponse(
                    p.getId(), 
                    p.getAspiranteId(), 
                    "Desconocido", 
                    p.getVacanteId(), 
                    "Desconocido", 
                    p.getEstatus(), 
                    p.getFechaPostulacion(), 
                    p.getObservaciones(),
                    p.getCartaDeclaratoriaId(),
                    p.getCvCompletado(),
                    p.getDocumentosCompletos(),
                    // ✅ NUEVOS CAMPOS
                    p.getCalificacionConocimientos(),
                    p.getCalificacionPsicometrica(),
                    p.getCalificacionEntrevista(),
                    p.getEstatusFinalSeleccion(),
                    p.getDictamenFinal()
                ));
    }
}