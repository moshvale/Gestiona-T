package mx.ine.gestiona_t.modules.documentos.repository;

import mx.ine.gestiona_t.modules.documentos.model.Documento;
import mx.ine.gestiona_t.modules.documentos.model.enums.EstatusDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface DocumentoRepository extends JpaRepository<Documento, UUID> {
    
    List<Documento> findByAspiranteId(UUID aspiranteId);
    
    List<Documento> findByFolio(String folio);
    
    @Query("SELECT COUNT(d) FROM Documento d WHERE d.aspiranteId = :aspiranteId AND d.estatus = :estatus")
    long countByAspiranteIdAndEstatus(@Param("aspiranteId") UUID aspiranteId, 
                                       @Param("estatus") EstatusDocumento estatus);
    
    @Query("SELECT d FROM Documento d WHERE d.aspiranteId = :aspiranteId ORDER BY d.fechaCarga DESC")
    List<Documento> findByAspiranteIdOrderByFechaCargaDesc(@Param("aspiranteId") UUID aspiranteId);

    // Evitar duplicados: buscar por aspirante y ruta de almacenamiento
    java.util.List<Documento> findByAspiranteIdAndStoragePath(UUID aspiranteId, String storagePath);

    // Evitar dos documentos del mismo tipo por aspirante cuando no corresponde duplicarlos
    java.util.List<Documento> findByAspiranteIdAndTipoDocumento(UUID aspiranteId, mx.ine.gestiona_t.modules.documentos.model.enums.TipoDocumento tipoDocumento);

    Optional<Documento> findFirstByExpedienteLaboral_IdOrderByFechaCargaDesc(UUID expedienteLaboralId);
}