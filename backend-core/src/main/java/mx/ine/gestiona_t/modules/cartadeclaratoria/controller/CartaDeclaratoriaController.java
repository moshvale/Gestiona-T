package mx.ine.gestiona_t.modules.cartadeclaratoria.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auth.service.JwtService;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request.AceptarBloqueRequest;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.request.FirmarCartaRequest;
import mx.ine.gestiona_t.modules.cartadeclaratoria.dto.response.*;
import mx.ine.gestiona_t.modules.cartadeclaratoria.service.CartaDeclaratoriaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/carta-declaratoria")
public class CartaDeclaratoriaController {

    private static final Logger log = LoggerFactory.getLogger(CartaDeclaratoriaController.class);

    private final CartaDeclaratoriaService cartaService;
    private final JwtService jwtService;

    public CartaDeclaratoriaController(CartaDeclaratoriaService cartaService, JwtService jwtService) {
        this.cartaService = cartaService;
        this.jwtService = jwtService;
    }

    /**
     * Obtiene la carta declaratoria de un aspirante por su aspiranteId (Uso exclusivo de Analistas).
     * Ruta: GET /api/v1/carta-declaratoria/por-aspirante/{aspiranteId}
     */
    @GetMapping("/por-aspirante/{aspiranteId}")
    public ResponseEntity<CartaDeclaratoriaResponse> obtenerCartaPorAspiranteId(@PathVariable UUID aspiranteId) {
        log.info("GET /api/v1/carta-declaratoria/por-aspirante/{}", aspiranteId);
        try {
            return ResponseEntity.ok(cartaService.obtenerCartaPorAspiranteId(aspiranteId).block());
        } catch (Exception e) {
            log.warn("⚠️ No se encontró carta para aspiranteId {}: {}", aspiranteId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene la carta declaratoria de un aspirante por su folio (Uso del Analista).
     * Ruta: GET /api/v1/carta-declaratoria/por-folio/{folio}
     */
    @GetMapping("/por-folio/{folio}")
    public ResponseEntity<CartaDeclaratoriaResponse> obtenerCartaPorFolio(@PathVariable String folio) {
        log.info("GET /api/v1/carta-declaratoria/por-folio/{}", folio);
        try {
            CartaDeclaratoriaResponse response = cartaService.obtenerCarta(folio).block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("⚠️ No se encontró carta para folio {}: {}", folio, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{folio}/iniciar")
    public ResponseEntity<CartaDeclaratoriaResponse> iniciarCarta(
            @PathVariable String folio,
            @RequestHeader("Authorization") String authHeader) {
        UUID aspiranteId = jwtService.extractAspiranteId(extractToken(authHeader));
        log.info("POST /api/v1/carta-declaratoria/{}/iniciar - Aspirante: {}", folio, aspiranteId);
        CartaDeclaratoriaResponse response = cartaService.iniciarCarta(aspiranteId, folio).block();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{folio}")
    public ResponseEntity<CartaDeclaratoriaResponse> obtenerCarta(@PathVariable String folio) {
        log.info("GET /api/v1/carta-declaratoria/{}", folio);
        return ResponseEntity.ok(cartaService.obtenerCarta(folio).block());
    }

    @GetMapping("/{folio}/bloques")
    public ResponseEntity<List<BloqueResponse>> obtenerBloques(@PathVariable String folio) {
        log.info("GET /api/v1/carta-declaratoria/{}/bloques", folio);
        return ResponseEntity.ok(cartaService.obtenerBloques(folio).block());
    }

    @PostMapping("/{folio}/aceptar-bloque")
    public ResponseEntity<AceptacionResponse> aceptarBloque(
            @PathVariable String folio,
            @Valid @RequestBody AceptarBloqueRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/carta-declaratoria/{}/aceptar-bloque - Bloque: {}", folio, request.bloqueId());
        AceptacionResponse response = cartaService.aceptarBloque(folio, request, ip, userAgent).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{folio}/aceptar-todos")
    public ResponseEntity<CartaDeclaratoriaResponse> aceptarTodos(
            @PathVariable String folio,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/carta-declaratoria/{}/aceptar-todos", folio);
        CartaDeclaratoriaResponse response = cartaService.aceptarTodosBloques(folio, ip, userAgent).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{folio}/validar-externo")
    public ResponseEntity<Boolean> validarExterno(
            @PathVariable String folio,
            @RequestParam String curp) {
        log.info("POST /api/v1/carta-declaratoria/{}/validar-externo", folio);
        return ResponseEntity.ok(cartaService.ejecutarValidacionesExternas(folio, curp).block());
    }

    @GetMapping("/{folio}/validaciones")
    public ResponseEntity<List<ValidacionExternaResponse>> obtenerValidaciones(@PathVariable String folio) {
        log.info("GET /api/v1/carta-declaratoria/{}/validaciones", folio);
        return ResponseEntity.ok(cartaService.obtenerValidaciones(folio).block());
    }

    @GetMapping("/{folio}/estatus")
    public ResponseEntity<EstatusCartaResponse> obtenerEstatus(@PathVariable String folio) {
        log.info("GET /api/v1/carta-declaratoria/{}/estatus", folio);
        return ResponseEntity.ok(cartaService.obtenerEstatus(folio).block());
    }

    @PostMapping("/{folio}/firmar")
    public ResponseEntity<CartaDeclaratoriaResponse> firmarCarta(
            @PathVariable String folio,
            @Valid @RequestBody FirmarCartaRequest request,
            HttpServletRequest httpRequest) {
        String ip = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        log.info("POST /api/v1/carta-declaratoria/{}/firmar - Metodo: {}", folio, request.metodoFirma());
        CartaDeclaratoriaResponse response = cartaService.firmarCarta(folio, request, ip, userAgent).block();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{folio}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> obtenerPdf(@PathVariable String folio) {
        log.info("GET /api/v1/carta-declaratoria/{}/pdf", folio);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=carta-declaratoria-" + folio + ".pdf")
                .body(cartaService.obtenerPdf(folio).block());
    }

    @GetMapping(value = "/{folio}/pdf/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(@PathVariable String folio) {
        log.info("GET /api/v1/carta-declaratoria/{}/pdf/download", folio);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=carta-declaratoria-" + folio + ".pdf")
                .body(cartaService.obtenerPdf(folio).block());
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        throw new RuntimeException("Token no proporcionado");
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

    /**
     * ✅ NUEVO: Obtiene el PDF de la carta declaratoria de un aspirante por su ID (Uso exclusivo de Analistas).
     * Ruta: GET /api/v1/carta-declaratoria/por-aspirante/{aspiranteId}/pdf
     */
    @GetMapping(value = "/por-aspirante/{aspiranteId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> obtenerPdfPorAspiranteId(@PathVariable UUID aspiranteId) {
        log.info("GET /api/v1/carta-declaratoria/por-aspirante/{}/pdf", aspiranteId);
        try {
            byte[] pdfBytes = cartaService.obtenerPdfPorAspiranteId(aspiranteId).block();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=carta-declaratoria-" + aspiranteId + ".pdf")
                    .body(pdfBytes);
        } catch (Exception e) {
            log.warn("⚠️ No se pudo generar el PDF para aspiranteId {}: {}", aspiranteId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}