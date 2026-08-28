package mx.ine.gestiona_t.modules.documentos.controller;

import mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository;
import mx.ine.gestiona_t.modules.auth.service.JwtService;
import mx.ine.gestiona_t.modules.documentos.dto.request.ValidacionRequest;
import mx.ine.gestiona_t.modules.documentos.dto.response.DocumentoResponse;
import mx.ine.gestiona_t.modules.documentos.dto.response.UploadResponse;
import mx.ine.gestiona_t.modules.documentos.dto.response.ValidacionResponse;
import mx.ine.gestiona_t.modules.documentos.service.DocumentoService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.net.URLConnection;

@CrossOrigin(origins = {"http://10.15.0.59:3007", "http://localhost:3007"}, allowCredentials = "true")
@RestController
@RequestMapping("/api/v1/documentos")
public class DocumentoController {

    private static final Logger log = LoggerFactory.getLogger(DocumentoController.class);

    private final DocumentoService documentoService;
    private final JwtService jwtService;
    private final AspiranteRepository aspiranteRepository;

    public DocumentoController(DocumentoService documentoService, JwtService jwtService, AspiranteRepository aspiranteRepository) {
        this.documentoService = documentoService;
        this.jwtService = jwtService;
        this.aspiranteRepository = aspiranteRepository;
    }

    @PostMapping(value = "/expediente-laboral/{expedienteLaboralId}/soporte", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> cargarSoporteExpediente(
            @RequestParam("file") MultipartFile file,
            @PathVariable UUID expedienteLaboralId) {
        try {
            return ResponseEntity.ok(documentoService.cargarSoporteExpediente(file, expedienteLaboralId).block());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error al cargar soporte del expediente laboral {}: {}", expedienteLaboralId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Sube un documento (CV, INE, o documentos de contratación) a MinIO.
     * Ruta: POST /api/v1/documentos/upload
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> cargarDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tipo", required = false, defaultValue = "CV") String tipo,
            @RequestParam(value = "esTambienDomicilio", required = false, defaultValue = "false") boolean esTambienDomicilio,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam(value = "aspiranteId", required = false) UUID aspiranteIdParam) {
        
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                log.error("❌ No hay autenticación en SecurityContext");
                return ResponseEntity.status(403).build();
            }

            boolean esAspirante = authentication.getAuthorities().stream()
                    .map(authority -> authority.getAuthority())
                    .anyMatch("ROLE_ASPIRANTE"::equals);

            UUID usuarioId = UUID.fromString(authentication.getPrincipal().toString());
            UUID aspiranteId = usuarioId;
            String folio = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    folio = jwtService.extractFolio(token);
                } catch (Exception e) {
                    log.warn("No se pudo extraer folio del token: {}", e.getMessage());
                }
            }

            // ✅ Lógica para Analistas/Admins que suben documentos en nombre de un aspirante
            if (!esAspirante) {
                if (aspiranteIdParam == null) {
                    log.warn("❌ Carga interna sin aspiranteId. Usuario: {}", usuarioId);
                    return ResponseEntity.badRequest().build();
                }
                aspiranteId = aspiranteIdParam;
                folio = aspiranteRepository.findById(aspiranteId)
                        .map(aspirante -> aspirante.getFolio())
                        .orElse(null);
                
                if (folio == null) {
                    log.warn("❌ Aspirante no encontrado para carga interna: {}", aspiranteId);
                    return ResponseEntity.notFound().build();
                }
            } else if (folio == null || folio.isEmpty()) {
                folio = "FOLIO_" + aspiranteId.toString().substring(0, 8);
            }

            log.info("📤 POST /api/v1/documentos/upload - Aspirante: {} | Tipo: {} | EsDomicilio: {} | Archivo: {}", 
                     aspiranteId, tipo, esTambienDomicilio, file.getOriginalFilename());
            
            UploadResponse response = documentoService.cargarDocumento(file, aspiranteId, folio, tipo, esTambienDomicilio).block();
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Error al procesar upload: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtiene la lista de documentos del aspirante autenticado.
     * Ruta: GET /api/v1/documentos
     */
    @GetMapping
    public ResponseEntity<List<DocumentoResponse>> obtenerMisDocumentos() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(403).build();
            }
            UUID aspiranteId = UUID.fromString(authentication.getPrincipal().toString());
            List<DocumentoResponse> response = documentoService.obtenerDocumentosAspirante(aspiranteId).block();
            return ResponseEntity.ok(response != null ? response : List.of());
        } catch (Exception e) {
            log.error("❌ Error al obtener documentos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtiene los detalles de un documento específico.
     * Ruta: GET /api/v1/documentos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentoResponse> obtenerDocumento(@PathVariable UUID id) {
        try {
            DocumentoResponse response = documentoService.obtenerDocumento(id).block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener documento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Elimina un documento.
     * Ruta: DELETE /api/v1/documentos/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDocumento(@PathVariable UUID id) {
        try {
            documentoService.eliminarDocumento(id).block();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("❌ Error al eliminar documento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Descarga un documento.
     * Ruta: GET /api/v1/documentos/{id}/download
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> descargarDocumento(@PathVariable UUID id) {
        try {
            byte[] content = documentoService.descargarDocumento(id).block();
            String nombreArchivo = documentoService.obtenerDocumento(id).block().nombreArchivo();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
        } catch (Exception e) {
            log.error("❌ Error al descargar documento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Visualiza un documento en el navegador (inline, sin forzar descarga).
     * Ruta: GET /api/v1/documentos/{id}/view
     */
    @GetMapping(value = "/{id}/view", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> visualizarDocumento(@PathVariable UUID id) {
        log.info("👁️ GET /api/v1/documentos/{}/view", id);
        try {
            byte[] contenido = documentoService.descargarDocumento(id).block();
            if (contenido == null || contenido.length == 0) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(detectarTipoContenido(documentoService.obtenerDocumento(id).block().nombreArchivo()))
                    .contentLength(contenido.length)
                    .body(contenido);
        } catch (Exception e) {
            log.error("❌ Error al visualizar documento: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    private MediaType detectarTipoContenido(String nombreArchivo) {
        String tipo = URLConnection.guessContentTypeFromName(nombreArchivo);
        return tipo != null ? MediaType.parseMediaType(tipo) : MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * Valida o rechaza un documento específico (Uso exclusivo de Analistas).
     * Ruta: POST /api/v1/documentos/{id}/validar
     */
    @PostMapping("/{id}/validar")
    public ResponseEntity<ValidacionResponse> validarDocumento(
            @PathVariable UUID id,
            @Valid @RequestBody ValidacionRequest request) {
        log.info("📋 POST /api/v1/documentos/{}/validar (Manual) - Estatus: {} | Motivo: {}", 
                 id, request.estatus(), request.motivo());
        ValidacionResponse response = documentoService.validarDocumentoManual(id, request.estatus(), request.motivo()).block();
        return ResponseEntity.ok(response);
    }

    /**
     * Valida todos los documentos de un aspirante por su folio.
     * Ruta: POST /api/v1/documentos/validar-todos/{folio}
     */
    @PostMapping("/validar-todos/{folio}")
    public ResponseEntity<ValidacionResponse> validarTodos(@PathVariable String folio) {
        try {
            ValidacionResponse response = documentoService.validarTodosDocumentos(folio).block();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al validar todos los documentos: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtiene los documentos de un aspirante por su folio (Uso exclusivo de Analistas).
     * Ruta: GET /api/v1/documentos/por-folio/{folio}
     */
    @GetMapping("/por-folio/{folio}")
    public ResponseEntity<List<DocumentoResponse>> obtenerDocumentosPorFolio(@PathVariable String folio) {
        log.info("📂 GET /api/v1/documentos/por-folio/{} (Consulta de Analista)", folio);
        try {
            List<DocumentoResponse> response = documentoService.obtenerDocumentosPorFolio(folio).block();
            if (response == null) {
                return ResponseEntity.ok(List.of());
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ Error al obtener documentos por folio {}: {}", folio, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Obtiene los documentos de un aspirante por su ID (Uso exclusivo de Analistas).
     * Ruta: GET /api/v1/documentos/por-aspirante/{aspiranteId}
     */
    @GetMapping("/por-aspirante/{aspiranteId}")
    public ResponseEntity<List<DocumentoResponse>> obtenerDocumentosPorAspiranteId(@PathVariable UUID aspiranteId) {
        log.info("📂 GET /api/v1/documentos/por-aspirante/{} (Consulta de Analista)", aspiranteId);
        try {
            List<DocumentoResponse> response = documentoService.obtenerDocumentosPorAspiranteId(aspiranteId).block();
            return ResponseEntity.ok(response != null ? response : List.of());
        } catch (Exception e) {
            log.error("❌ Error al obtener documentos por aspiranteId {}: {}", aspiranteId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}