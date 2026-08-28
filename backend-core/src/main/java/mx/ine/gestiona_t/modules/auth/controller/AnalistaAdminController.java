package mx.ine.gestiona_t.modules.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mx.ine.gestiona_t.modules.auth.dto.request.CrearAnalistaRequest;
import mx.ine.gestiona_t.modules.auth.dto.response.AnalistaResumenDTO;
import mx.ine.gestiona_t.modules.auth.dto.response.MensajeResponse;
import mx.ine.gestiona_t.modules.auth.service.AnalistaService;
import mx.ine.gestiona_t.modules.auditoria.annotation.Auditable;
import mx.ine.gestiona_t.modules.auditoria.model.enums.NivelSeveridad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/analistas")
public class AnalistaAdminController {

    private static final Logger log = LoggerFactory.getLogger(AnalistaAdminController.class);
    private final AnalistaService analistaService;

    @Value("${admin.portal.secret-path}")
    private String secretPath;

    public AnalistaAdminController(AnalistaService analistaService) {
        this.analistaService = analistaService;
    }

    private void validarSecret(String portalSecret, HttpServletRequest request) {
        if (portalSecret == null || !portalSecret.equals(secretPath)) {
            log.warn("🚨 Intento de acceso con secret path inválido desde IP: {}", request.getRemoteAddr());
            throw new RuntimeException("Acceso denegado");
        }
    }

    @PostMapping("/crear")
    @PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")
    @Auditable(
        categoria = "ADMINISTRACION", 
        tipo = "ALTA_ANALISTA",  // ✅ CORREGIDO: Con comillas
        severidad = NivelSeveridad.WARNING, 
        recurso = "Analista", 
        descripcion = "Alta de nuevo analista del INE por administrador"
    )
    public ResponseEntity<MensajeResponse> crearAnalista(
            @Valid @RequestBody CrearAnalistaRequest request,
            @RequestHeader(value = "X-Admin-Portal-Secret", required = false) String portalSecret,
            HttpServletRequest httpRequest) {
        
        validarSecret(portalSecret, httpRequest);
        UUID adminId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        
        try {
            MensajeResponse response = analistaService.crearAnalista(
                request, 
                adminId, 
                httpRequest.getRemoteAddr(), 
                httpRequest.getHeader("User-Agent")
            );
            return ResponseEntity.status(201).body(response);
        } catch (RuntimeException e) {
            log.error("❌ Error al crear analista: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeResponse(e.getMessage(), 400));
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<List<AnalistaResumenDTO>> listarAnalistas(
            @RequestHeader(value = "X-Admin-Portal-Secret", required = false) String portalSecret,
            HttpServletRequest httpRequest) {
        
        validarSecret(portalSecret, httpRequest);
        return ResponseEntity.ok(analistaService.listarAnalistas());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN_SISTEMA')")
    @Auditable(
        categoria = "ADMINISTRACION", 
        tipo = "BAJA_ANALISTA",  // ✅ CORREGIDO: Con comillas
        severidad = NivelSeveridad.WARNING, 
        recurso = "Analista", 
        descripcion = "Desactivación de analista del INE"
    )
    public ResponseEntity<MensajeResponse> desactivarAnalista(
            @PathVariable UUID id,
            @RequestHeader(value = "X-Admin-Portal-Secret", required = false) String portalSecret,
            HttpServletRequest httpRequest) {
        
        validarSecret(portalSecret, httpRequest);
        UUID adminId = UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getName());
        
        try {
            return ResponseEntity.ok(analistaService.desactivarAnalista(id, adminId));
        } catch (RuntimeException e) {
            log.error("❌ Error al desactivar analista: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MensajeResponse(e.getMessage(), 400));
        }
    }
}