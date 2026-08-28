package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.auth.repository.AspiranteRepository;
import mx.ine.gestiona_t.modules.documentos.dto.response.ExpedienteResponse;
import mx.ine.gestiona_t.modules.documentos.model.Documento;
import mx.ine.gestiona_t.modules.documentos.model.ExpedienteDigital;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusExpediente;
import mx.ine.gestiona_t.modules.documentos.repository.DocumentoRepository;
import mx.ine.gestiona_t.modules.documentos.repository.ExpedienteDigitalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
public class ExpedienteService {
    
    private static final Logger log = LoggerFactory.getLogger(ExpedienteService.class);
    private final ExpedienteDigitalRepository expedienteRepository;
    private final DocumentoRepository documentoRepository;
    private final AspiranteRepository aspiranteRepository;
    
    public ExpedienteService(ExpedienteDigitalRepository expedienteRepository,
                              DocumentoRepository documentoRepository,
                              AspiranteRepository aspiranteRepository) {
        this.expedienteRepository = expedienteRepository;
        this.documentoRepository = documentoRepository;
        this.aspiranteRepository = aspiranteRepository;
    }
    
    @Transactional
    public ExpedienteDigital obtenerOCrear(UUID aspiranteId, String folio) {
        return expedienteRepository.findByAspiranteId(aspiranteId)
            .orElseGet(() -> {
                String folioExpediente = folio;
                if (folioExpediente == null || folioExpediente.isBlank()) {
                    folioExpediente = aspiranteRepository.findById(aspiranteId)
                        .map(aspirante -> aspirante.getFolio())
                        .orElseThrow(() -> new IllegalStateException(
                            "No se encontró el aspirante " + aspiranteId + " para crear su expediente"));
                }

                ExpedienteDigital nuevo = new ExpedienteDigital();
                nuevo.setAspiranteId(aspiranteId);
                nuevo.setFolio(folioExpediente);
                nuevo.setDocumentosTotales(0);
                nuevo.setDocumentosValidados(0);
                nuevo.setDocumentosRechazados(0);
                nuevo.setDocumentosEnRevision(0);
                nuevo.setEstatusGeneral(EstatusExpediente.INCOMPLETO);
                nuevo.setSfpVerificado(false);
                return expedienteRepository.save(nuevo);
            });
    }
    
    @Transactional
    public void actualizarExpediente(UUID aspiranteId) {
        ExpedienteDigital expediente = obtenerOCrear(aspiranteId, null);
        List<Documento> documentos = documentoRepository.findByAspiranteId(aspiranteId);
        
        int total = documentos.size();
        int validados = (int) documentos.stream()
            .filter(d -> d.getEstatus() == EstatusDocumento.VALIDADO_AUTOMATICO ||
                        d.getEstatus() == EstatusDocumento.VALIDADO_ASISTIDO ||
                        d.getEstatus() == EstatusDocumento.VALIDADO_MANUAL)
            .count();
        int rechazados = (int) documentos.stream()
            .filter(d -> d.getEstatus() == EstatusDocumento.RECHAZADO)
            .count();
        int enRevision = (int) documentos.stream()
            .filter(d -> d.getEstatus() == EstatusDocumento.EN_REVISION_MANUAL)
            .count();
        
        expediente.setDocumentosTotales(total);
        expediente.setDocumentosValidados(validados);
        expediente.setDocumentosRechazados(rechazados);
        expediente.setDocumentosEnRevision(enRevision);
        expediente.setEstatusGeneral(calcularEstatusGeneral(total, validados, rechazados, enRevision));
        
        expedienteRepository.save(expediente);
        log.info("Expediente actualizado para aspirante {}: total={}, validados={}, rechazados={}", 
                 aspiranteId, total, validados, rechazados);
    }
    
    private EstatusExpediente calcularEstatusGeneral(int total, int validados, int rechazados, int enRevision) {
        if (rechazados > 0) return EstatusExpediente.CON_RECHAZOS;
        if (enRevision > 0) return EstatusExpediente.VALIDACION_PARCIAL;
        if (validados == total && total > 0) return EstatusExpediente.COMPLETO_VALIDADO;
        if (validados > 0) return EstatusExpediente.EN_VALIDACION;
        return EstatusExpediente.INCOMPLETO;
    }
    
    public ExpedienteResponse obtenerExpedienteResponse(String folio) {
        ExpedienteDigital exp = expedienteRepository.findByFolio(folio)
            .orElseThrow(() -> new RuntimeException("Expediente no encontrado"));
        
        return new ExpedienteResponse(
            exp.getId(), exp.getFolio(), exp.getAspiranteId(),
            exp.getDocumentosTotales(), exp.getDocumentosValidados(),
            exp.getDocumentosRechazados(), exp.getDocumentosEnRevision(),
            exp.getEstatusGeneral(), exp.isSfpVerificado(), exp.getSfpHabilitado(),
            exp.getFechaUltimaActualizacion()
        );
    }
}