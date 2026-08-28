package mx.ine.gestiona_t.modules.matching.controller;

import jakarta.servlet.http.HttpServletRequest;
import mx.ine.gestiona_t.modules.auth.service.JwtService;
import mx.ine.gestiona_t.modules.matching.service.MatchingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/matching")
public class MatchingController {

    private static final Logger log = LoggerFactory.getLogger(MatchingController.class);
    private final MatchingService matchingService;
    private final JwtService jwtService;

    public MatchingController(MatchingService matchingService, JwtService jwtService) {
        this.matchingService = matchingService;
        this.jwtService = jwtService;
    }

    @PostMapping("/evaluar")
    public ResponseEntity<Map<String, Object>> evaluarMiCv(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autorización no proporcionado");
        }
        
        UUID aspiranteId = jwtService.extractAspiranteId(authHeader.substring(7));
        String perfilPuesto = request.getOrDefault("perfil_puesto", "Desarrollador de Software con experiencia en Java y Python, capaz de trabajar en equipo y con conocimientos en bases de datos relacionales."); // Perfil por defecto para la demo

        log.info("POST /api/v1/matching/evaluar - Aspirante: {}", aspiranteId);
        Map<String, Object> resultado = matchingService.evaluarCv(aspiranteId, perfilPuesto);
        
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/mi-resultado")
    public ResponseEntity<Map<String, Object>> obtenerMiResultado(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        UUID aspiranteId = jwtService.extractAspiranteId(authHeader.substring(7));
        
        return ResponseEntity.ok(matchingService.obtenerResultado(aspiranteId));
    }
}