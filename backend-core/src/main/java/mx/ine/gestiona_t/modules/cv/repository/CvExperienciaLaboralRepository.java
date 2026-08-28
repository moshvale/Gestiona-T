package mx.ine.gestiona_t.modules.cv.repository;

import mx.ine.gestiona_t.modules.cv.model.CvExperienciaLaboral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CvExperienciaLaboralRepository extends JpaRepository<CvExperienciaLaboral, UUID> {
    List<CvExperienciaLaboral> findByCvId(UUID cvId);
    void deleteByCvId(UUID cvId);
}