package mx.ine.gestiona_t.modules.documentos.repository;

import mx.ine.gestiona_t.modules.documentos.model.RevisionManual;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RevisionManualRepository extends JpaRepository<RevisionManual, Long> {
    
    Optional<RevisionManual> findByDocumentoId(UUID documentoId);
    
    @Query("SELECT r FROM RevisionManual r WHERE r.estatus = :estatus ORDER BY r.prioridad DESC, r.createdAt ASC")
    List<RevisionManual> findByEstatusOrderByPrioridad(@Param("estatus") EstatusRevision estatus);
    
    List<RevisionManual> findByAnalistaId(UUID analistaId);
    
    @Query("SELECT COUNT(r) FROM RevisionManual r WHERE r.estatus = 'PENDIENTE'")
    long countPendientes();
}