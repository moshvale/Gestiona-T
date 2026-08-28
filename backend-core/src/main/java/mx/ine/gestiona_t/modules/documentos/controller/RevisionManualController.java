package mx.ine.gestiona_t.modules.documentos.controller;

import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auth.service.JwtService;
import mx.ine.gestiona_t.modules.documentos.dto.request.DictamenRevisionRequest;
import mx.ine.gestiona_t.modules.documentos.dto.response.RevisionResponse;
import mx.ine.gestiona_t.modules.documentos.service.DocumentoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/revisiones")
public class RevisionManualController {
    
    private static final Logger log = LoggerFactory.getLogger(RevisionManualController.class);
    private final DocumentoService documentoService;
    private final JwtService jwtService;
    
    public RevisionManualController(DocumentoService documentoService, JwtService jwtService) {
        this.documentoService = documentoService;
        this.jwtService = jwtService;
    }
    
    @GetMapping("/pendientes")
    public ResponseEntity<Mono<List<RevisionResponse>>> obtenerPendientes() {
        log.info("GET /api/v1/admin/revisiones/pendientes");
        return ResponseEntity.ok(documentoService.obtenerRevisionesPendientes());
    }
    
    @PostMapping("/{revisionId}/dictaminar")
    public ResponseEntity<Mono<RevisionResponse>> dictaminar(
            @PathVariable Long revisionId,
            @Valid @RequestBody DictamenRevisionRequest request,
            @RequestHeader("Authorization") String authHeader) {
        
        UUID analistaId = jwtService.extractAspiranteId(extractToken(authHeader));
        log.info("POST /api/v1/admin/revisiones/{}/dictaminar - Analista: {}", revisionId, analistaId);
        
        return ResponseEntity.ok(documentoService.dictaminarRevision(revisionId, analistaId, request));
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token no proporcionado");
    }
}