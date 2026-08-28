package mx.ine.gestiona_t.modules.firma.repository;

import mx.ine.gestiona_t.modules.firma.model.SelloDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SelloDigitalRepository extends JpaRepository<SelloDigital, Long> {
    
    Optional<SelloDigital> findByDocumentoFirmadoId(UUID documentoFirmadoId);
    
    boolean existsByDocumentoFirmadoId(UUID documentoFirmadoId);
}