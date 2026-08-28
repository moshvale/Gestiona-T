package mx.ine.gestiona_t.modules.auditoria.controller;

import mx.ine.gestiona_t.modules.auditoria.model.ConfiguracionRetencion;
import mx.ine.gestiona_t.modules.auditoria.repository.ConfiguracionRetencionRepository;
import mx.ine.gestiona_t.modules.auditoria.service.AuditoriaService;
import mx.ine.gestiona_t.modules.auditoria.service.CadenaHashService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/auditoria")
public class AdminAuditoriaController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminAuditoriaController.class);
    
    private final ConfiguracionRetencionRepository retencionRepository;
    private final CadenaHashService cadenaHashService;
    private final AuditoriaService auditoriaService;
    
    public AdminAuditoriaController(ConfiguracionRetencionRepository retencionRepository,
                                      CadenaHashService cadenaHashService,
                                      AuditoriaService auditoriaService) {
        this.retencionRepository = retencionRepository;
        this.cadenaHashService = cadenaHashService;
        this.auditoriaService = auditoriaService;
    }
    
    @GetMapping("/configuracion")
    public ResponseEntity<List<ConfiguracionRetencion>> obtenerConfiguracion() {
        log.info("GET /api/v1/admin/auditoria/configuracion");
        return ResponseEntity.ok(retencionRepository.findAll());
    }
    
    @PutMapping("/configuracion/retencion/{categoria}")
    public ResponseEntity<ConfiguracionRetencion> actualizarRetencion(
            @PathVariable String categoria,
            @RequestParam int anios) {
        log.info("PUT /api/v1/admin/auditoria/configuracion/retencion/{} = {} anios", categoria, anios);
        
        ConfiguracionRetencion config = retencionRepository.findByCategoria(categoria)
            .orElseGet(() -> {
                ConfiguracionRetencion nuevo = new ConfiguracionRetencion();
                nuevo.setCategoria(categoria);
                nuevo.setActivo(true);
                return nuevo;
            });
        
        config.setAniosRetencion(anios);
        return ResponseEntity.ok(retencionRepository.save(config));
    }
    
    @PostMapping("/verificar-integridad")
    public ResponseEntity<?> verificarIntegridad() {
        log.info("POST /api/v1/admin/auditoria/verificar-integridad");
        return ResponseEntity.ok(auditoriaService.verificarIntegridadCadena());
    }
    
    @GetMapping("/cadena/longitud")
    public ResponseEntity<Long> longitudCadena() {
        return ResponseEntity.ok(cadenaHashService.getLongitudCadena());
    }
    
    @GetMapping("/cadena/ultimo-hash")
    public ResponseEntity<String> ultimoHash() {
        return ResponseEntity.ok(cadenaHashService.obtenerUltimoHash());
    }
}