package mx.ine.gestiona_t.modules.firma.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.firma.dto.request.FirmarBiometricaRequest;
import mx.ine.gestiona_t.modules.firma.dto.request.FirmarFEARequest;
import mx.ine.gestiona_t.modules.firma.dto.request.FirmarOTPRequest;
import mx.ine.gestiona_t.modules.firma.dto.response.FirmaResponse;
import mx.ine.gestiona_t.modules.firma.dto.response.MetadataFirmaResponse;
import mx.ine.gestiona_t.modules.firma.dto.response.ValidacionFirmaResponse;
import mx.ine.gestiona_t.modules.firma.service.FirmaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/firmas")
public class FirmaController {
    
    private static final Logger log = LoggerFactory.getLogger(FirmaController.class);
    private final FirmaService firmaService;
    
    public FirmaController(FirmaService firmaService) {
        this.firmaService = firmaService;
    }
    
    @PostMapping("/firmar-fea")
    public ResponseEntity<Mono<FirmaResponse>> firmarFEA(
            @Valid @RequestBody FirmarFEARequest request,
            HttpServletRequest httpRequest) {
        
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/firmas/firmar-fea - Documento: {}", request.nombreArchivo());
        
        return ResponseEntity.ok(firmaService.firmarConFEA(request, ip, userAgent));
    }
    
    @PostMapping("/firmar-biometrica")
    public ResponseEntity<Mono<FirmaResponse>> firmarBiometrica(
            @Valid @RequestBody FirmarBiometricaRequest request,
            HttpServletRequest httpRequest) {
        
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/firmas/firmar-biometrica - Documento: {}", request.nombreArchivo());
        
        return ResponseEntity.ok(firmaService.firmarConBiometria(request, ip, userAgent));
    }
    
    @PostMapping("/firmar-otp")
    public ResponseEntity<Mono<FirmaResponse>> firmarOTP(
            @Valid @RequestBody FirmarOTPRequest request,
            @RequestHeader(value = "X-Geolocalizacion", required = false) String geolocalizacion,
            @RequestHeader(value = "X-Dispositivo-Id", required = false) String dispositivoId,
            HttpServletRequest httpRequest) {
        
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/firmas/firmar-otp - Documento: {}", request.nombreArchivo());
        
        return ResponseEntity.ok(firmaService.firmarConOTP(request, ip, userAgent, geolocalizacion, dispositivoId));
    }
    
    @GetMapping("/{folioDocumento}")
    public ResponseEntity<Mono<FirmaResponse>> obtenerDocumento(@PathVariable String folioDocumento) {
        log.info("GET /api/v1/firmas/{}", folioDocumento);
        return ResponseEntity.ok(firmaService.obtenerDocumento(folioDocumento));
    }
    
    @GetMapping("/aspirante/{aspiranteId}")
    public ResponseEntity<Mono<List<FirmaResponse>>> obtenerDocumentosAspirante(
            @PathVariable UUID aspiranteId) {
        log.info("GET /api/v1/firmas/aspirante/{}", aspiranteId);
        return ResponseEntity.ok(firmaService.obtenerDocumentosAspirante(aspiranteId));
    }
    
    @GetMapping("/{folioDocumento}/validar")
    public ResponseEntity<Mono<ValidacionFirmaResponse>> validarFirma(@PathVariable String folioDocumento) {
        log.info("GET /api/v1/firmas/{}/validar", folioDocumento);
        return ResponseEntity.ok(firmaService.validarFirma(folioDocumento));
    }
    
    @GetMapping("/{folioDocumento}/metadata")
    public ResponseEntity<Mono<MetadataFirmaResponse>> obtenerMetadata(@PathVariable String folioDocumento) {
        log.info("GET /api/v1/firmas/{}/metadata", folioDocumento);
        return ResponseEntity.ok(firmaService.obtenerMetadata(folioDocumento));
    }
    
    @GetMapping(value = "/{folioDocumento}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Mono<byte[]>> obtenerPdf(@PathVariable String folioDocumento) {
        log.info("GET /api/v1/firmas/{}/pdf", folioDocumento);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + folioDocumento + ".pdf")
            .body(firmaService.obtenerPdfFirmado(folioDocumento));
    }
    
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}