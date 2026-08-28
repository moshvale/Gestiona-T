package mx.ine.gestiona_t.modules.cartadeclaratoria.controller;

import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.BloqueResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.service.BloquesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bloques-declaratorios")
public class BloquesController {
    
    private static final Logger log = LoggerFactory.getLogger(BloquesController.class);
    private final BloquesService bloquesService;
    
    public BloquesController(BloquesService bloquesService) {
        this.bloquesService = bloquesService;
    }
    
    @GetMapping
    public ResponseEntity<List<BloqueResponse>> obtenerTodos() {
        log.info("GET /api/v1/bloques-declaratorios");
        return ResponseEntity.ok(bloquesService.obtenerBloquesConEstatus(null));
    }
}