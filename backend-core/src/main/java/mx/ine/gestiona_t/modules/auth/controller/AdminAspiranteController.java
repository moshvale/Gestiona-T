package mx.ine.gestiona_t.modules.auth.controller;

import mx.ine.gestiona_t.modules.auth.model.Aspirante;
import mx.ine.gestiona_t.modules.auth.dto.response.AspiranteExpedienteResumenDTO;
import mx.ine.gestiona_t.modules.documentos.model.ExpedienteDigital;
import mx.ine.gestiona_t.modules.documentos.repository.ExpedienteDigitalRepository;
import mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/aspirantes")
public class AdminAspiranteController {

    private static final Logger log = LoggerFactory.getLogger(AdminAspiranteController.class);
    private final AspiranteRepository aspiranteRepository;
    private final ExpedienteDigitalRepository expedienteRepository;

    public AdminAspiranteController(AspiranteRepository aspiranteRepository,
                                    ExpedienteDigitalRepository expedienteRepository) {
        this.aspiranteRepository = aspiranteRepository;
        this.expedienteRepository = expedienteRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ANALISTA_UR', 'ROLE_ADMIN_SISTEMA')")
    public ResponseEntity<List<AspiranteExpedienteResumenDTO>> listarAspirantes() {
        List<AspiranteExpedienteResumenDTO> aspirantes = aspiranteRepository.findAll().stream()
            .filter(Aspirante::isActivo)
            .map(this::mapToResumen)
            .toList();

        return ResponseEntity.ok(aspirantes);
    }

    @GetMapping("/{folio}")
    public ResponseEntity<?> obtenerPorFolio(@PathVariable String folio) {
        log.info("GET /api/v1/admin/aspirantes/{}", folio);
        
        return aspiranteRepository.findByFolio(folio)
            .map(aspirante -> ResponseEntity.ok().body(Map.of(
                "folio", aspirante.getFolio(),
                "nombreCompleto", aspirante.getNombreCompleto(),
                "correoElectronico", aspirante.getCorreoElectronico(),
                "curp", aspirante.getCurp(),
                "rfc", aspirante.getRfc(),
                "telefonoMovil", aspirante.getTelefonoMovil(),
                "estatus", aspirante.getEstatus()
            )))
            .orElse(ResponseEntity.notFound().build());
    }

    private AspiranteExpedienteResumenDTO mapToResumen(Aspirante aspirante) {
        ExpedienteDigital expediente = expedienteRepository.findByAspiranteId(aspirante.getId()).orElse(null);
        return new AspiranteExpedienteResumenDTO(
            aspirante.getId(),
            aspirante.getFolio(),
            aspirante.getNombreCompleto(),
            aspirante.getCorreoElectronico(),
            expediente != null ? expediente.getEstatusGeneral().name() : "INCOMPLETO",
            expediente != null ? expediente.getDocumentosTotales() : 0,
            expediente != null ? expediente.getDocumentosValidados() : 0,
            expediente != null ? expediente.getDocumentosRechazados() : 0
        );
    }
}