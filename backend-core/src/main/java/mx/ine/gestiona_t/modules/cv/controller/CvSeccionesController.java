package mx.ine.gestiona_t.modules.cv.controller;

import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.cv.dto.request.*;
import mx.ine.gestiona_t.modules.cv.dto.response.*;
import mx.ine.gestiona_t.modules.cv.service.CvService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cv/{folio}")
public class CvSeccionesController {
    
    private static final Logger log = LoggerFactory.getLogger(CvSeccionesController.class);
    
    private final CvService cvService;
    
    public CvSeccionesController(CvService cvService) {
        this.cvService = cvService;
    }
    
    // ============ ESCOLARIDAD ============
    
    @PostMapping("/escolaridad")
    public ResponseEntity<Mono<EscolaridadResponse>> agregarEscolaridad(
            @PathVariable String folio,
            @Valid @RequestBody EscolaridadRequest request) {
        log.info("POST /api/v1/cv/{}/escolaridad", folio);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cvService.agregarEscolaridad(folio, request));
    }
    
    @PutMapping("/escolaridad/{id}")
    public ResponseEntity<Mono<EscolaridadResponse>> actualizarEscolaridad(
            @PathVariable String folio,
            @PathVariable UUID id,
            @Valid @RequestBody EscolaridadRequest request) {
        log.info("PUT /api/v1/cv/{}/escolaridad/{}", folio, id);
        return ResponseEntity.ok(cvService.actualizarEscolaridad(folio, id, request));
    }
    
    @DeleteMapping("/escolaridad/{id}")
    public ResponseEntity<Mono<Void>> eliminarEscolaridad(
            @PathVariable String folio,
            @PathVariable UUID id) {
        log.info("DELETE /api/v1/cv/{}/escolaridad/{}", folio, id);
        return ResponseEntity.ok(cvService.eliminarEscolaridad(folio, id));
    }
    
    // ============ EXPERIENCIA ============
    
    @PostMapping("/experiencia")
    public ResponseEntity<Mono<ExperienciaResponse>> agregarExperiencia(
            @PathVariable String folio,
            @Valid @RequestBody ExperienciaRequest request) {
        log.info("POST /api/v1/cv/{}/experiencia", folio);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cvService.agregarExperiencia(folio, request));
    }
    
    @PutMapping("/experiencia/{id}")
    public ResponseEntity<Mono<ExperienciaResponse>> actualizarExperiencia(
            @PathVariable String folio,
            @PathVariable UUID id,
            @Valid @RequestBody ExperienciaRequest request) {
        log.info("PUT /api/v1/cv/{}/experiencia/{}", folio, id);
        return ResponseEntity.ok(cvService.actualizarExperiencia(folio, id, request));
    }
    
    @DeleteMapping("/experiencia/{id}")
    public ResponseEntity<Mono<Void>> eliminarExperiencia(
            @PathVariable String folio,
            @PathVariable UUID id) {
        log.info("DELETE /api/v1/cv/{}/experiencia/{}", folio, id);
        return ResponseEntity.ok(cvService.eliminarExperiencia(folio, id));
    }
    
    // ============ CURSOS ============
    
    @PostMapping("/cursos")
    public ResponseEntity<Mono<CursoResponse>> agregarCurso(
            @PathVariable String folio,
            @Valid @RequestBody CursoRequest request) {
        log.info("POST /api/v1/cv/{}/cursos", folio);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cvService.agregarCurso(folio, request));
    }
    
    @PutMapping("/cursos/{id}")
    public ResponseEntity<Mono<CursoResponse>> actualizarCurso(
            @PathVariable String folio,
            @PathVariable UUID id,
            @Valid @RequestBody CursoRequest request) {
        log.info("PUT /api/v1/cv/{}/cursos/{}", folio, id);
        return ResponseEntity.ok(cvService.actualizarCurso(folio, id, request));
    }
    
    @DeleteMapping("/cursos/{id}")
    public ResponseEntity<Mono<Void>> eliminarCurso(
            @PathVariable String folio,
            @PathVariable UUID id) {
        log.info("DELETE /api/v1/cv/{}/cursos/{}", folio, id);
        return ResponseEntity.ok(cvService.eliminarCurso(folio, id));
    }
    
    // ============ HABILIDADES ============
    
    @PostMapping("/habilidades")
    public ResponseEntity<Mono<HabilidadResponse>> agregarHabilidad(
            @PathVariable String folio,
            @Valid @RequestBody HabilidadRequest request) {
        log.info("POST /api/v1/cv/{}/habilidades", folio);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(cvService.agregarHabilidad(folio, request));
    }
    
    @PutMapping("/habilidades/{id}")
    public ResponseEntity<Mono<HabilidadResponse>> actualizarHabilidad(
            @PathVariable String folio,
            @PathVariable UUID id,
            @Valid @RequestBody HabilidadRequest request) {
        log.info("PUT /api/v1/cv/{}/habilidades/{}", folio, id);
        return ResponseEntity.ok(cvService.actualizarHabilidad(folio, id, request));
    }
    
    @DeleteMapping("/habilidades/{id}")
    public ResponseEntity<Mono<Void>> eliminarHabilidad(
            @PathVariable String folio,
            @PathVariable UUID id) {
        log.info("DELETE /api/v1/cv/{}/habilidades/{}", folio, id);
        return ResponseEntity.ok(cvService.eliminarHabilidad(folio, id));
    }
}