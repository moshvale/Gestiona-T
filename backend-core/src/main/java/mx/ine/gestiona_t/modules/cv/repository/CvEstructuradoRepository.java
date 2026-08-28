package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.CvEstructurado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CvEstructuradoRepository extends JpaRepository<CvEstructurado, UUID> {
    
    Optional<CvEstructurado> findByFolio(String folio);
    
    Optional<CvEstructurado> findByAspiranteId(UUID aspiranteId);
    
    boolean existsByAspiranteId(UUID aspiranteId);
    
    @Query("SELECT c FROM CvEstructurado c WHERE c.aspiranteId = :aspiranteId AND c.completo = true")
    Optional<CvEstructurado> findCompletoByAspiranteId(@Param("aspiranteId") UUID aspiranteId);
}