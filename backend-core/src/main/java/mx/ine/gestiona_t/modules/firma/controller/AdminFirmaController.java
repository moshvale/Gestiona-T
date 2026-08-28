package mx.ine.gestiona_t.modules.firma.controller;

import mx.ine.gestiona_t.modules.firma.dto.response.FirmaResponse;
import mx.ine.gestiona_t.modules.firma.model.DocumentoFirmado;
import mx.ine.gestiona_t.modules.firma.model.enums.EstatusFirma;
import mx.ine.gestiona_t.modules.firma.model.enums.NivelFirma;
import mx.ine.gestiona_t.modules.firma.repository.DocumentoFirmadoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/firmas")
public class AdminFirmaController {
    
    private static final Logger log = LoggerFactory.getLogger(AdminFirmaController.class);
    private final DocumentoFirmadoRepository documentoRepository;
    
    public AdminFirmaController(DocumentoFirmadoRepository documentoRepository) {
        this.documentoRepository = documentoRepository;
    }
    
    @GetMapping
    public ResponseEntity<List<FirmaResponse>> listarTodas() {
        log.info("GET /api/v1/admin/firmas");
        List<FirmaResponse> firmas = documentoRepository.findAll().stream()
            .map(d -> mapToResponse(d, ""))
            .collect(Collectors.toList());
        return ResponseEntity.ok(firmas);
    }
    
    @GetMapping("/nivel/{nivel}")
    public ResponseEntity<List<FirmaResponse>> listarPorNivel(@PathVariable NivelFirma nivel) {
        log.info("GET /api/v1/admin/firmas/nivel/{}", nivel);
        long count = documentoRepository.countFirmadasPorNivel(nivel);
        log.info("Total firmas nivel {}: {}", nivel, count);
        return ResponseEntity.ok(List.of());
    }
    
    @GetMapping("/{folioDocumento}/detalle")
    public ResponseEntity<FirmaResponse> obtenerDetalle(@PathVariable String folioDocumento) {
        log.info("GET /api/v1/admin/firmas/{}/detalle", folioDocumento);
        DocumentoFirmado doc = documentoRepository.findByFolioDocumento(folioDocumento)
            .orElseThrow(() -> new RuntimeException("Documento no encontrado"));
        return ResponseEntity.ok(mapToResponse(doc, "Detalle completo"));
    }
    
    private FirmaResponse mapToResponse(DocumentoFirmado doc, String mensaje) {
        return new FirmaResponse(
            doc.getId(), doc.getFolioDocumento(), doc.getFolioAspirante(),
            doc.getNivelFirma(), doc.getEstatus(), doc.getNombreArchivo(),
            doc.getHashOriginal(), doc.getHashFirmado(),
            doc.getFechaSolicitud(), doc.getFechaFirma(), mensaje
        );
    }
}