package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.CvInstitucional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CvInstitucionalRepository extends JpaRepository<CvInstitucional, UUID> {
    Optional<CvInstitucional> findByAspiranteId(UUID aspiranteId);
    boolean existsByAspiranteId(UUID aspiranteId);
}