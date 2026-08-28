package mx.ine.gestiona_t.modules.documentos.service;

import mx.ine.gestiona_t.modules.documentos.model.Documento;
import mx.ine.gestiona_t.modules.documentos.model.RevisionManual;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusRevision;
import mx.ine.gestiona_t.modules.documentos.repository.RevisionManualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class ValidacionTipoCService {
    
    private static final Logger log = LoggerFactory.getLogger(ValidacionTipoCService.class);
    private final RevisionManualRepository revisionRepository;
    
    public ValidacionTipoCService(RevisionManualRepository revisionRepository) {
        this.revisionRepository = revisionRepository;
    }
    
    @Transactional
    public Map<String, Object> encolarParaRevision(Documento documento, int prioridad) {
        log.info("Encolando documento {} para revision manual", documento.getId());
        
        documento.setEstatus(EstatusDocumento.EN_REVISION_MANUAL);
        
        RevisionManual revision = new RevisionManual();
        revision.setDocumentoId(documento.getId());
        revision.setEstatus(EstatusRevision.PENDIENTE);
        revision.setPrioridad(prioridad);
        revisionRepository.save(revision);
        
        return Map.of(
            "exitoso", true,
            "mensaje", "Documento encolado para revision manual",
            "metodo", "REVISION_MANUAL",
            "revisionId", revision.getId()
        );
    }
    
    @Transactional
    public Map<String, Object> dictaminar(Long revisionId, UUID analistaId, 
                                            EstatusRevision estatus, String dictamen, String motivo) {
        RevisionManual revision = revisionRepository.findById(revisionId)
            .orElseThrow(() -> new RuntimeException("Revision no encontrada"));
        
        revision.setAnalistaId(analistaId);
        revision.setEstatus(estatus);
        revision.setDictamen(dictamen);
        revision.setMotivo(motivo);
        revision.setFechaDictamen(LocalDateTime.now());
        revisionRepository.save(revision);
        
        return Map.of(
            "exitoso", true,
            "mensaje", "Dictamen registrado",
            "revisionId", revisionId
        );
    }
}