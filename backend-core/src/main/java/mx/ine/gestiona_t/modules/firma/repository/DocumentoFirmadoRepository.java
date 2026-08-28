package mx.ine.gestiona_t.modules.firma.repository;

import mx.ine.gestiona_t.modules.firma.model.DocumentoFirmado;
import mx.ine.gestiona_t.modules.firma.model.enums.EstatusFirma;
import mx.ine.gestiona_t.modules.firma.model.enums.NivelFirma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentoFirmadoRepository extends JpaRepository<DocumentoFirmado, UUID> {
    
    Optional<DocumentoFirmado> findByFolioDocumento(String folioDocumento);
    
    List<DocumentoFirmado> findByAspiranteId(UUID aspiranteId);
    
    List<DocumentoFirmado> findByFolioAspirante(String folioAspirante);
    
    @Query("SELECT d FROM DocumentoFirmado d WHERE d.aspiranteId = :aspiranteId " +
           "AND d.estatus = :estatus ORDER BY d.createdAt DESC")
    List<DocumentoFirmado> findByAspiranteIdAndEstatus(
        @Param("aspiranteId") UUID aspiranteId,
        @Param("estatus") EstatusFirma estatus
    );
    
    @Query("SELECT d FROM DocumentoFirmado d WHERE d.estatus = 'SOLICITADA' " +
           "AND d.fechaExpiracion < :ahora")
    List<DocumentoFirmado> findExpirados(@Param("ahora") LocalDateTime ahora);
    
    @Query("SELECT COUNT(d) FROM DocumentoFirmado d WHERE d.nivelFirma = :nivel AND d.estatus = 'FIRMADA'")
    long countFirmadasPorNivel(@Param("nivel") NivelFirma nivel);
}