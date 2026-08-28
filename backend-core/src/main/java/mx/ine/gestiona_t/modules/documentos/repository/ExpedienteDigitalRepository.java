package mx.ine.gestiona_t.modules.documentos.repository;

import mx.ine.gestiona_t.modules.documentos.model.ExpedienteDigital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExpedienteDigitalRepository extends JpaRepository<ExpedienteDigital, UUID> {
    
    Optional<ExpedienteDigital> findByAspiranteId(UUID aspiranteId);
    
    Optional<ExpedienteDigital> findByFolio(String folio);
    
    boolean existsByAspiranteId(UUID aspiranteId);
}