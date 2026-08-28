package mx.ine.gestiona_t.modules.matching.repository;

import mx.ine.gestiona_t.modules.matching.model.ResultadoMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ResultadoMatchingRepository extends JpaRepository<ResultadoMatching, UUID> {
    
    // ✅ CAMBIO CLAVE: Obtiene el ÚLTIMO resultado ordenado por fecha descendente
    Optional<ResultadoMatching> findFirstByAspiranteIdOrderByFechaEvaluacionDesc(UUID aspiranteId);
}