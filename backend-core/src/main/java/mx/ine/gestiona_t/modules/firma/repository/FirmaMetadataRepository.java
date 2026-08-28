package mx.ine.gestiona_t.modules.firma.repository;

import mx.ine.gestiona_t.modules.firma.model.FirmaMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FirmaMetadataRepository extends JpaRepository<FirmaMetadata, Long> {
    
    Optional<FirmaMetadata> findByDocumentoFirmadoId(UUID documentoFirmadoId);
    
    boolean existsByDocumentoFirmadoId(UUID documentoFirmadoId);
}