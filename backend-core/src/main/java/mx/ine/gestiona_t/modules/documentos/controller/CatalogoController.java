package mx.ine.gestiona_t.modules.documentos.controller;

import mx.ine.gestiona_t.modules.documentos.dto.response.CatalogoResponse;
import mx.ine.gestiona_t.modules.documentos.service.CatalogoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {
    
    private static final Logger log = LoggerFactory.getLogger(CatalogoController.class);
    private final CatalogoService catalogoService;
    
    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }
    
    @GetMapping("/instituciones")
    public ResponseEntity<List<CatalogoResponse>> obtenerInstituciones(
            @RequestParam(required = false) String tipo) {
        
        log.info("GET /api/v1/catalogos/instituciones - tipo: {}", tipo);
        
        if (tipo != null && !tipo.isEmpty()) {
            return ResponseEntity.ok(catalogoService.obtenerInstituciones(tipo));
        }
        return ResponseEntity.ok(catalogoService.obtenerInstituciones("EMS"));
    }
    
    @GetMapping("/instituciones/buscar")
    public ResponseEntity<List<CatalogoResponse>> buscarInstituciones(
            @RequestParam String nombre) {
        
        log.info("GET /api/v1/catalogos/instituciones/buscar - nombre: {}", nombre);
        return ResponseEntity.ok(catalogoService.buscarInstituciones(nombre));
    }
}