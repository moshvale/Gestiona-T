package mx.ine.gestiona_t.modules.documentos.controller;

import mx.ine.gestiona_t.modules.auth.service.JwtService;
import mx.ine.gestiona_t.modules.documentos.dto.response.ExpedienteResponse;
import mx.ine.gestiona_t.modules.documentos.service.ExpedienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/expediente")
public class ExpedienteController {
    
    private static final Logger log = LoggerFactory.getLogger(ExpedienteController.class);
    private final ExpedienteService expedienteService;
    private final JwtService jwtService;
    
    public ExpedienteController(ExpedienteService expedienteService, JwtService jwtService) {
        this.expedienteService = expedienteService;
        this.jwtService = jwtService;
    }
    
    @GetMapping("/{folio}")
    public ResponseEntity<ExpedienteResponse> obtenerExpediente(@PathVariable String folio) {
        log.info("GET /api/v1/expediente/{}", folio);
        return ResponseEntity.ok(expedienteService.obtenerExpedienteResponse(folio));
    }
    
    @GetMapping("/mi-expediente")
    public ResponseEntity<ExpedienteResponse> obtenerMiExpediente(
            @RequestHeader("Authorization") String authHeader) {
        
        String folio = jwtService.extractFolio(extractToken(authHeader));
        log.info("GET /api/v1/expediente/mi-expediente - Folio: {}", folio);
        return ResponseEntity.ok(expedienteService.obtenerExpedienteResponse(folio));
    }
    
    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token no proporcionado");
    }
}