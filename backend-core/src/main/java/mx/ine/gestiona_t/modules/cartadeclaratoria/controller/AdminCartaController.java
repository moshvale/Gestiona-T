package mx.ine.gestiona_t.modules.cartadeclaratoria.controller;

import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.CartaDeclaratoriaResponse;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.CartaDeclaratoria;
import mx.ine.gestiona_t.modules.cartadeclaratoria.model.enums.EstatusCarta;
import mx.ine.gestiona_t.modules.cartadeclaratoria.repository.CartaDeclaratoriaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/cartas-declaratorias")
public class AdminCartaController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminCartaController.class);
    private final CartaDeclaratoriaRepository cartaRepository;
    
    public AdminCartaController(CartaDeclaratoriaRepository cartaRepository) {
        this.cartaRepository = cartaRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<CartaDeclaratoriaResponse>> listarTodas() {
        log.info("GET /api/v1/admin/cartas-declaratorias");
        
        List<CartaDeclaratoriaResponse> cartas = cartaRepository.findAll().stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(cartas);
    }
    
    @GetMapping("/estatus/{estatus}")
    public ResponseEntity<List<CartaDeclaratoriaResponse>> listarPorEstatus(
            @PathVariable EstatusCarta estatus) {
        log.info("GET /api/v1/admin/cartas-declaratorias/estatus/{}", estatus);
        
        List<CartaDeclaratoriaResponse> cartas = cartaRepository.findByEstatus(estatus).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(cartas);
    }
    
    @GetMapping("/{folio}/detalle")
    public ResponseEntity<CartaDeclaratoriaResponse> obtenerDetalle(@PathVariable String folio) {
        log.info("GET /api/v1/admin/cartas-declaratorias/{}/detalle", folio);
        
        CartaDeclaratoria carta = cartaRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("Carta no encontrada"));
        
        return ResponseEntity.ok(mapToResponse(carta));
    }
    
    private CartaDeclaratoriaResponse mapToResponse(CartaDeclaratoria carta) {
        return new CartaDeclaratoriaResponse(
            carta.getId(),
            carta.getAspiranteId(),
            carta.getFolio(),
            carta.getFolioCarta(),
            carta.getVersion(),
            carta.getEstatus(),
            carta.getMetodoFirma(),
            carta.getFechaAceptacionCompleta(),
            carta.getFechaFirma(),
            carta.getCreatedAt()
        );
    }
}