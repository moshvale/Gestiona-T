package mx.ine.gestiona_t.modules.cv.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auth.service.JwtService;
import mx.ine.gestiona_t.modules.cv.dto.request.CvInstitucionalRequest;
import mx.ine.gestiona_t.modules.cv.dto.response.CvInstitucionalResponse;
import mx.ine.gestiona_t.modules.cv.service.CvInstitucionalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cv")
public class CvInstitucionalController {

    private static final Logger log = LoggerFactory.getLogger(CvInstitucionalController.class);

    private final CvInstitucionalService cvService;
    private final JwtService jwtService;

    public CvInstitucionalController(CvInstitucionalService cvService, JwtService jwtService) {
        this.cvService = cvService;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<CvInstitucionalResponse> guardarOActualizarCv(
            @Valid @RequestBody CvInstitucionalRequest request,
            HttpServletRequest httpRequest) {
        
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autorización no proporcionado o inválido");
        }
        String token = authHeader.substring(7);
        UUID aspiranteId = jwtService.extractAspiranteId(token);

        log.info("POST /api/v1/cv - Guardando/Actualizando CV para aspirante: {}", aspiranteId);

        // ✅ Llamada síncrona directa, la transacción funciona perfectamente
        CvInstitucionalResponse response = cvService.guardarOActualizarCv(aspiranteId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<CvInstitucionalResponse> obtenerMiCv(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autorización no proporcionado o inválido");
        }
        String token = authHeader.substring(7);
        UUID aspiranteId = jwtService.extractAspiranteId(token);

        log.info("GET /api/v1/cv - Obteniendo CV para aspirante: {}", aspiranteId);

        try {
            CvInstitucionalResponse response = cvService.obtenerCvPorAspirante(aspiranteId);
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("GET /api/v1/cv - No existe CV, retornando CV vacío para aspirante: {}", aspiranteId);
                return ResponseEntity.ok(defaultEmptyCv(aspiranteId));
            }
            throw ex;
        }
    }

    @GetMapping("/aspirante/{aspiranteId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<CvInstitucionalResponse> obtenerCvDeAspirante(@PathVariable UUID aspiranteId) {
        log.info("GET /api/v1/cv/aspirante/{} - Consulta de analista", aspiranteId);
        return ResponseEntity.ok(cvService.obtenerCvPorAspirante(aspiranteId));
    }

    private CvInstitucionalResponse defaultEmptyCv(UUID aspiranteId) {
        return new CvInstitucionalResponse(
                null,
                aspiranteId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                0,
                null,
                null,
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> descargarCvPdf(HttpServletRequest httpRequest) {
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token de autorización no proporcionado o inválido");
        }
        String token = authHeader.substring(7);
        UUID aspiranteId = jwtService.extractAspiranteId(token);
        
        log.info("GET /api/v1/cv/pdf - Generando PDF para aspirante: {}", aspiranteId);
        
        byte[] pdfBytes = cvService.generarPdfCv(aspiranteId);
        
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "CV_Institucional_GestionaT.pdf");
        
        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }
}